package com.shopmanagement.ipdservice.clinical;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.accommodation.client.AccommodationClient;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.BedOccupancyDto;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class IpdAdmissionService {

    private final IpdAdmissionRepository admissionRepository;
    private final IpdTransferRepository transferRepository;
    private final AccommodationClient accommodationClient;

    public IpdAdmissionService(
            IpdAdmissionRepository admissionRepository,
            IpdTransferRepository transferRepository,
            AccommodationClient accommodationClient) {
        this.admissionRepository = admissionRepository;
        this.transferRepository = transferRepository;
        this.accommodationClient = accommodationClient;
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
        a.setStatus("REQUESTED");
        a.setNotes(incoming.getNotes());
        a.setCreatedBy(TenantContext.currentActor());
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
                reserveOnly);
        a.setBedId(bedId);
        a.setOccupancyId(occ.getId());
        a.setStatus(reserveOnly ? "RESERVED" : "ADMITTED");
        if (!reserveOnly) {
            a.setAdmittedAt(LocalDateTime.now());
        }
        return admissionRepository.save(a);
    }

    @Transactional
    public IpdTransfer transfer(Long admissionId, Long toBedId, String reason) {
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
                false);
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
        IpdTransfer saved = transferRepository.save(t);

        a.setBedId(toBedId);
        a.setOccupancyId(occ.getId());
        a.setStatus("TRANSFERRED");
        admissionRepository.save(a);
        return saved;
    }

    @Transactional
    public IpdAdmission discharge(Long admissionId, String notes) {
        IpdAdmission a = get(admissionId);
        if (a.getOccupancyId() != null) {
            accommodationClient.releaseOccupancy(a.getOccupancyId());
        }
        a.setStatus("DISCHARGED");
        a.setDischargedAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            a.setNotes((a.getNotes() == null ? "" : a.getNotes() + "\n") + notes.trim());
        }
        return admissionRepository.save(a);
    }

    public List<IpdTransfer> transfersFor(Long admissionId) {
        get(admissionId);
        return transferRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByTransferredAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
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
        return admissionRepository.save(a);
    }

    public java.util.Map<String, Object> tpaClaimExport(Long admissionId) {
        IpdAdmission a = get(admissionId);
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("claimFormat", "SUGAMFLOW_TPA_STUB_V1");
        out.put("admissionNo", a.getAdmissionNo());
        out.put("patientId", a.getPatientId());
        out.put("patientName", a.getPatientName());
        out.put("diagnosis", a.getDiagnosis());
        out.put("admittedAt", a.getAdmittedAt());
        out.put("dischargedAt", a.getDischargedAt());
        out.put("tpaName", a.getTpaName());
        out.put("preauthStatus", a.getTpaPreauthStatus());
        out.put("preauthRef", a.getTpaPreauthRef());
        out.put("approvedAmount", a.getTpaApprovedAmount());
        out.put("notes", a.getTpaNotes());
        out.put("exportedAt", java.time.LocalDateTime.now().toString());
        return out;
    }

    private String nextAdmissionNo(Long tenantId, String shopId) {
        String prefix = "IPD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        long seq = admissionRepository.countByTenantIdAndShopIdAndAdmissionNoStartingWith(tenantId, shopId, prefix) + 1;
        return prefix + String.format("%04d", seq);
    }
}
