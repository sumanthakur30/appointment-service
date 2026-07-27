package com.shopmanagement.ipdservice.clinical;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.accommodation.client.AccommodationClient;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.BedOccupancyDto;
import com.shopmanagement.ipdservice.hl7.Hl7AdtService;
import com.shopmanagement.ipdservice.mar.MarService;
import com.shopmanagement.ipdservice.support.AdmissionStatus;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class IpdAdmissionService {

    private final IpdAdmissionRepository admissionRepository;
    private final IpdTransferRepository transferRepository;
    private final AccommodationClient accommodationClient;
    private final MarService marService;
    private final Hl7AdtService hl7AdtService;
    private final String defaultClaimFormat;

    public IpdAdmissionService(
            IpdAdmissionRepository admissionRepository,
            IpdTransferRepository transferRepository,
            AccommodationClient accommodationClient,
            MarService marService,
            Hl7AdtService hl7AdtService,
            @Value("${ipd.tpa.default-claim-format:GENERIC}") String defaultClaimFormat) {
        this.admissionRepository = admissionRepository;
        this.transferRepository = transferRepository;
        this.accommodationClient = accommodationClient;
        this.marService = marService;
        this.hl7AdtService = hl7AdtService;
        this.defaultClaimFormat = defaultClaimFormat == null || defaultClaimFormat.isBlank()
                ? "GENERIC" : defaultClaimFormat.trim().toUpperCase();
    }

    public List<IpdAdmission> list() {
        return admissionRepository.findByTenantIdAndShopIdOrderByCreatedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    public IpdAdmission get(Long id) {
        return admissionRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }

    @Transactional
    public IpdAdmission create(IpdAdmission incoming) {
        if (incoming.getPatientId() == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        if (incoming.getEncounterId() != null) {
            var existing = admissionRepository.findByTenantIdAndShopIdAndEncounterId(
                    tenantId, shopId, incoming.getEncounterId());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        IpdAdmission a = new IpdAdmission();
        a.setTenantId(tenantId);
        a.setShopId(shopId);
        a.setAdmissionNo(nextAdmissionNo(tenantId, shopId));
        a.setPatientId(incoming.getPatientId());
        a.setPatientName(incoming.getPatientName());
        a.setPatientGender(normalizeGender(incoming.getPatientGender()));
        a.setPatientAgeYears(incoming.getPatientAgeYears());
        a.setEncounterId(incoming.getEncounterId());
        a.setConsultantDoctorId(incoming.getConsultantDoctorId());
        a.setDepartment(incoming.getDepartment());
        a.setDiagnosis(incoming.getDiagnosis());
        a.setAdmissionReason(incoming.getAdmissionReason());
        a.setExpectedStayDays(incoming.getExpectedStayDays());
        a.setWardPreference(incoming.getWardPreference());
        a.setPriority(incoming.getPriority() == null || incoming.getPriority().isBlank()
                ? "ROUTINE" : incoming.getPriority().trim().toUpperCase());
        a.setInsuranceRef(incoming.getInsuranceRef());
        a.setCorporateRef(incoming.getCorporateRef());
        a.setPackageCode(incoming.getPackageCode());
        a.setPackageAmount(incoming.getPackageAmount());
        a.setEmergency(incoming.isEmergency());
        a.setDepositAmount(incoming.getDepositAmount());
        if (AdmissionStatus.WAITLISTED.equalsIgnoreCase(incoming.getStatus())) {
            a.setStatus(AdmissionStatus.WAITLISTED);
            a.setWaitlistedAt(LocalDateTime.now());
            a.setExpectedAdmitAt(incoming.getExpectedAdmitAt());
            a.setWaitlistRank(incoming.getWaitlistRank() != null ? incoming.getWaitlistRank() : 1);
        } else {
            a.setStatus(AdmissionStatus.REQUESTED);
            a.setExpectedAdmitAt(incoming.getExpectedAdmitAt());
        }
        a.setNotes(incoming.getNotes());
        a.setCreatedBy(TenantContext.currentActor());
        return admissionRepository.save(a);
    }

    public List<IpdAdmission> waitlist() {
        return admissionRepository.findByTenantIdAndShopIdAndStatusOrderByWaitlistRankAscWaitlistedAtAsc(
                TenantContext.requireTenantId(),
                TenantContext.requireShopId(),
                AdmissionStatus.WAITLISTED);
    }

    @Transactional
    public IpdAdmission placeOnWaitlist(Long admissionId, Integer rank, LocalDateTime expectedAdmitAt) {
        IpdAdmission a = get(admissionId);
        if (AdmissionStatus.ADMITTED.equalsIgnoreCase(a.getStatus())
                || AdmissionStatus.DISCHARGED.equalsIgnoreCase(a.getStatus())
                || AdmissionStatus.CANCELLED.equalsIgnoreCase(a.getStatus())) {
            throw new IllegalStateException("Cannot waitlist status " + a.getStatus());
        }
        a.setStatus(AdmissionStatus.WAITLISTED);
        a.setWaitlistedAt(LocalDateTime.now());
        a.setWaitlistRank(rank != null && rank > 0 ? rank : 1);
        a.setExpectedAdmitAt(expectedAdmitAt);
        return admissionRepository.save(a);
    }

    @Transactional
    public IpdAdmission promoteFromWaitlist(Long admissionId) {
        IpdAdmission a = get(admissionId);
        if (!AdmissionStatus.WAITLISTED.equalsIgnoreCase(a.getStatus())) {
            throw new IllegalStateException("Admission is not waitlisted");
        }
        a.setStatus(AdmissionStatus.REQUESTED);
        return admissionRepository.save(a);
    }

    @Transactional
    public IpdAdmission allocateBed(Long admissionId, Long bedId, boolean reserveOnly, LocalDateTime expectedDischarge) {
        IpdAdmission a = get(admissionId);
        if ("DISCHARGED".equalsIgnoreCase(a.getStatus()) || "CANCELLED".equalsIgnoreCase(a.getStatus())) {
            throw new IllegalStateException("Cannot allocate bed for status " + a.getStatus());
        }
        BedOccupancyDto occ = accommodationClient.allocateBed(
                bedId,
                a.getAdmissionNo(),
                String.valueOf(a.getPatientId()),
                a.getPatientName(),
                expectedDischarge,
                reserveOnly,
                a.getPatientGender(),
                a.getPatientAgeYears());
        a.setBedId(bedId);
        a.setOccupancyId(occ.getId());
        a.setStatus(reserveOnly ? AdmissionStatus.RESERVED : AdmissionStatus.ADMITTED);
        if (!reserveOnly) {
            a.setAdmittedAt(LocalDateTime.now());
        }
        a.setWaitlistedAt(null);
        a.setWaitlistRank(null);
        IpdAdmission saved = admissionRepository.save(a);
        if (!reserveOnly) {
            hl7AdtService.emitAdmit(saved);
        }
        return saved;
    }

    @Transactional
    public IpdTransfer transfer(Long admissionId, Long toBedId, String reason) {
        return transfer(admissionId, toBedId, reason, null);
    }

    @Transactional
    public IpdTransfer transfer(Long admissionId, Long toBedId, String reason, Long checklistSubmissionId) {
        IpdAdmission a = get(admissionId);
        if (a.getBedId() == null || a.getOccupancyId() == null) {
            throw new IllegalStateException("Admission has no active bed");
        }
        if (!"ADMITTED".equalsIgnoreCase(a.getStatus()) && !"TRANSFERRED".equalsIgnoreCase(a.getStatus())) {
            throw new IllegalStateException("Transfer allowed only for admitted patients");
        }
        Long fromBedId = a.getBedId();
        accommodationClient.releaseOccupancy(a.getOccupancyId());
        BedOccupancyDto occ = accommodationClient.allocateBed(
                toBedId,
                a.getAdmissionNo(),
                String.valueOf(a.getPatientId()),
                a.getPatientName(),
                null,
                false,
                a.getPatientGender(),
                a.getPatientAgeYears());
        accommodationClient.setBedStatus(fromBedId, "CLEANING");

        IpdTransfer t = new IpdTransfer();
        t.setTenantId(a.getTenantId());
        t.setShopId(a.getShopId());
        t.setAdmissionId(a.getId());
        t.setFromBedId(fromBedId);
        t.setToBedId(toBedId);
        t.setReason(reason);
        t.setStatus("COMPLETED");
        t.setTransferredAt(LocalDateTime.now());
        t.setApprovedBy(TenantContext.currentActor());
        t.setCreatedBy(TenantContext.currentActor());
        t.setChecklistSubmissionId(checklistSubmissionId);
        IpdTransfer saved = transferRepository.save(t);

        a.setBedId(toBedId);
        a.setOccupancyId(occ.getId());
        a.setStatus("TRANSFERRED");
        IpdAdmission savedAdmission = admissionRepository.save(a);
        hl7AdtService.emitTransfer(savedAdmission);
        return saved;
    }

    @Transactional
    public IpdAdmission discharge(Long admissionId, String notes) {
        IpdAdmission a = get(admissionId);
        marService.reconcileOnDischarge(admissionId);
        if (a.getOccupancyId() != null) {
            accommodationClient.releaseOccupancy(a.getOccupancyId());
        }
        a.setStatus(AdmissionStatus.DISCHARGED);
        a.setDischargedAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            a.setNotes((a.getNotes() == null ? "" : a.getNotes() + "\n") + notes.trim());
        }
        IpdAdmission saved = admissionRepository.save(a);
        hl7AdtService.emitDischarge(saved);
        return saved;
    }

    public List<IpdTransfer> transfersFor(Long admissionId) {
        get(admissionId);
        return transferRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByTransferredAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    public List<com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationBedDto> eligibleBeds(
            Long admissionId, boolean availableOnly) {
        IpdAdmission a = get(admissionId);
        return accommodationClient.listEligibleBeds(
                a.getPatientGender(), a.getPatientAgeYears(), availableOnly, null, null);
    }

    @Transactional
    public IpdAdmission updateTpa(Long admissionId, IpdAdmission incoming) {
        IpdAdmission a = get(admissionId);
        if (incoming.getTpaName() != null) {
            a.setTpaName(incoming.getTpaName().trim());
        }
        if (incoming.getTpaPreauthStatus() != null) {
            a.setTpaPreauthStatus(incoming.getTpaPreauthStatus().trim().toUpperCase());
        }
        if (incoming.getTpaPreauthRef() != null) {
            a.setTpaPreauthRef(incoming.getTpaPreauthRef().trim());
        }
        if (incoming.getTpaApprovedAmount() != null) {
            a.setTpaApprovedAmount(incoming.getTpaApprovedAmount());
        }
        if (incoming.getTpaNotes() != null) {
            a.setTpaNotes(incoming.getTpaNotes());
        }
        if (incoming.getTpaClaimFormat() != null && !incoming.getTpaClaimFormat().isBlank()) {
            a.setTpaClaimFormat(incoming.getTpaClaimFormat().trim().toUpperCase());
        }
        return admissionRepository.save(a);
    }

    public String resolveClaimFormat(IpdAdmission a) {
        if (a.getTpaClaimFormat() != null && !a.getTpaClaimFormat().isBlank()) {
            return a.getTpaClaimFormat().trim().toUpperCase();
        }
        return defaultClaimFormat;
    }

    public java.util.Map<String, Object> tpaClaimExport(Long admissionId) {
        IpdAdmission a = get(admissionId);
        String format = resolveClaimFormat(a);
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("claimFormat", format);
        out.put("admissionNo", a.getAdmissionNo());
        out.put("patientId", a.getPatientId() != null ? String.valueOf(a.getPatientId()) : "");
        out.put("patientName", a.getPatientName());
        out.put("diagnosis", a.getDiagnosis());
        out.put("primaryIcdCode", a.getPrimaryIcdCode());
        out.put("primaryIcdDesc", a.getPrimaryIcdDesc());
        out.put("secondaryIcdCodes", a.getSecondaryIcdCodes());
        out.put("admittedAt", a.getAdmittedAt() != null ? a.getAdmittedAt().toString() : "");
        out.put("dischargedAt", a.getDischargedAt() != null ? a.getDischargedAt().toString() : "");
        out.put("tpaName", a.getTpaName());
        out.put("preauthStatus", a.getTpaPreauthStatus());
        out.put("preauthRef", a.getTpaPreauthRef());
        out.put("approvedAmount", a.getTpaApprovedAmount() != null ? a.getTpaApprovedAmount().toPlainString() : "");
        out.put("packageCode", a.getPackageCode());
        out.put("notes", a.getTpaNotes());
        out.put("exportedAt", java.time.LocalDateTime.now().toString());
        return out;
    }

    private String nextAdmissionNo(Long tenantId, String shopId) {
        String prefix = "IPD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        long seq = admissionRepository.countByTenantIdAndShopIdAndAdmissionNoStartingWith(tenantId, shopId, prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private static String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        String g = gender.trim().toUpperCase();
        return switch (g) {
            case "M", "MALE", "MAN", "BOY" -> "MALE";
            case "F", "FEMALE", "WOMAN", "GIRL" -> "FEMALE";
            case "O", "OTHER", "X", "NON_BINARY", "NONBINARY" -> "OTHER";
            default -> g;
        };
    }
}
