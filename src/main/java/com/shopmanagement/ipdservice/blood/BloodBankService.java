package com.shopmanagement.ipdservice.blood;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class BloodBankService {

    private static final Set<String> REQUEST_STATUSES =
            Set.of("REQUESTED", "CROSSMATCHED", "ISSUED", "RETURNED", "CANCELLED");

    private final BloodUnitRepository unitRepository;
    private final BloodRequestRepository requestRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final boolean enabled;

    public BloodBankService(
            BloodUnitRepository unitRepository,
            BloodRequestRepository requestRepository,
            IpdAdmissionRepository admissionRepository,
            @Value("${ipd.blood.enabled:true}") boolean enabled) {
        this.unitRepository = unitRepository;
        this.requestRepository = requestRepository;
        this.admissionRepository = admissionRepository;
        this.enabled = enabled;
    }

    public List<BloodUnit> listUnits() {
        requireEnabled();
        return unitRepository.findByTenantIdAndShopIdOrderByExpiresAtAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    @Transactional
    public BloodUnit addUnit(BloodUnit incoming) {
        requireEnabled();
        if (incoming.getUnitCode() == null || incoming.getUnitCode().isBlank()) {
            throw new IllegalArgumentException("unitCode is required");
        }
        if (incoming.getBloodGroup() == null || incoming.getBloodGroup().isBlank()) {
            throw new IllegalArgumentException("bloodGroup is required");
        }
        BloodUnit row = new BloodUnit();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setUnitCode(incoming.getUnitCode().trim().toUpperCase(Locale.ROOT));
        row.setBloodGroup(incoming.getBloodGroup().trim().toUpperCase(Locale.ROOT));
        row.setComponent(blankDefault(incoming.getComponent(), "PRBC").toUpperCase(Locale.ROOT));
        row.setStatus("AVAILABLE");
        row.setCollectedAt(incoming.getCollectedAt() != null ? incoming.getCollectedAt() : LocalDateTime.now());
        row.setExpiresAt(incoming.getExpiresAt());
        row.setDonorRef(incoming.getDonorRef());
        row.setNotes(incoming.getNotes());
        return unitRepository.save(row);
    }

    public List<BloodRequest> listRequests() {
        requireEnabled();
        return requestRepository.findByTenantIdAndShopIdOrderByRequestedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    public List<BloodRequest> listForAdmission(Long admissionId) {
        requireEnabled();
        return requestRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByRequestedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    @Transactional
    public BloodRequest createRequest(BloodRequest incoming) {
        requireEnabled();
        if (incoming.getBloodGroup() == null || incoming.getBloodGroup().isBlank()) {
            throw new IllegalArgumentException("bloodGroup is required");
        }
        Long patientId = incoming.getPatientId();
        Long admissionId = incoming.getAdmissionId();
        if (admissionId != null) {
            IpdAdmission admission = admissionRepository
                    .findByIdAndTenantIdAndShopId(
                            admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                    .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
            patientId = admission.getPatientId();
        }
        if (patientId == null) {
            throw new IllegalArgumentException("patientId or admissionId is required");
        }
        BloodRequest row = new BloodRequest();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(admissionId);
        row.setOtBookingId(incoming.getOtBookingId());
        row.setPatientId(patientId);
        row.setBloodGroup(incoming.getBloodGroup().trim().toUpperCase(Locale.ROOT));
        row.setComponent(blankDefault(incoming.getComponent(), "PRBC").toUpperCase(Locale.ROOT));
        row.setUnitsRequested(incoming.getUnitsRequested() <= 0 ? 1 : incoming.getUnitsRequested());
        row.setClinicalIndication(incoming.getClinicalIndication());
        row.setStatus("REQUESTED");
        row.setRequestedBy(TenantContext.currentActor());
        row.setRequestedAt(LocalDateTime.now());
        row.setNotes(incoming.getNotes());
        return requestRepository.save(row);
    }

    @Transactional
    public BloodRequest advance(Long id, String status, Long unitId) {
        requireEnabled();
        BloodRequest row = requestRepository
                .findByIdAndTenantIdAndShopId(id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Blood request not found"));
        String st = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!REQUEST_STATUSES.contains(st)) {
            throw new IllegalArgumentException("Invalid status; use " + REQUEST_STATUSES);
        }
        if ("CROSSMATCHED".equals(st)) {
            BloodUnit unit = resolveUnit(row, unitId);
            unit.setStatus("RESERVED");
            unitRepository.save(unit);
            row.setMatchedUnitId(unit.getId());
            row.setCrossmatchedAt(LocalDateTime.now());
        } else if ("ISSUED".equals(st)) {
            if (row.getMatchedUnitId() == null && unitId == null) {
                throw new IllegalArgumentException("Crossmatch a unit before issue");
            }
            BloodUnit unit = resolveUnit(row, unitId != null ? unitId : row.getMatchedUnitId());
            unit.setStatus("ISSUED");
            unitRepository.save(unit);
            row.setMatchedUnitId(unit.getId());
            row.setIssuedAt(LocalDateTime.now());
        } else if ("RETURNED".equals(st)) {
            if (row.getMatchedUnitId() != null) {
                unitRepository
                        .findByIdAndTenantIdAndShopId(
                                row.getMatchedUnitId(),
                                TenantContext.requireTenantId(),
                                TenantContext.requireShopId())
                        .ifPresent(u -> {
                            u.setStatus("AVAILABLE");
                            unitRepository.save(u);
                        });
            }
            row.setReturnedAt(LocalDateTime.now());
        } else if ("CANCELLED".equals(st) && row.getMatchedUnitId() != null) {
            unitRepository
                    .findByIdAndTenantIdAndShopId(
                            row.getMatchedUnitId(),
                            TenantContext.requireTenantId(),
                            TenantContext.requireShopId())
                    .ifPresent(u -> {
                        if ("RESERVED".equalsIgnoreCase(u.getStatus())) {
                            u.setStatus("AVAILABLE");
                            unitRepository.save(u);
                        }
                    });
        }
        row.setStatus(st);
        return requestRepository.save(row);
    }

    private BloodUnit resolveUnit(BloodRequest request, Long unitId) {
        if (unitId != null) {
            BloodUnit unit = unitRepository
                    .findByIdAndTenantIdAndShopId(
                            unitId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                    .orElseThrow(() -> new IllegalArgumentException("Blood unit not found"));
            if (!request.getBloodGroup().equalsIgnoreCase(unit.getBloodGroup())) {
                throw new IllegalArgumentException("Blood group mismatch");
            }
            return unit;
        }
        return unitRepository
                .findFirstByTenantIdAndShopIdAndBloodGroupAndComponentAndStatusOrderByExpiresAtAsc(
                        TenantContext.requireTenantId(),
                        TenantContext.requireShopId(),
                        request.getBloodGroup(),
                        request.getComponent(),
                        "AVAILABLE")
                .orElseThrow(() -> new IllegalArgumentException("No available unit for "
                        + request.getBloodGroup() + " " + request.getComponent()));
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new IllegalStateException("Blood bank is disabled (ipd.blood.enabled=false)");
        }
    }

    private static String blankDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
