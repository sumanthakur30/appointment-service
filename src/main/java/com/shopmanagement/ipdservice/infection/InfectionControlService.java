package com.shopmanagement.ipdservice.infection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.accommodation.client.AccommodationClient;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class InfectionControlService {

    private final InfectionIsolationRepository isolationRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final AccommodationClient accommodationClient;
    private final HaiEventRepository haiEventRepository;
    private final boolean haiEnabled;

    public InfectionControlService(
            InfectionIsolationRepository isolationRepository,
            IpdAdmissionRepository admissionRepository,
            AccommodationClient accommodationClient,
            HaiEventRepository haiEventRepository,
            @Value("${ipd.ipc.hai-registry-enabled:true}") boolean haiEnabled) {
        this.isolationRepository = isolationRepository;
        this.admissionRepository = admissionRepository;
        this.accommodationClient = accommodationClient;
        this.haiEventRepository = haiEventRepository;
        this.haiEnabled = haiEnabled;
    }

    public List<InfectionIsolation> listActive() {
        return isolationRepository.findByTenantIdAndShopIdAndActiveTrueOrderByStartedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    public List<InfectionIsolation> listForAdmission(Long admissionId) {
        assertAdmission(admissionId);
        return isolationRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByStartedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    @Transactional
    public InfectionIsolation start(InfectionIsolation incoming) {
        if (incoming.getAdmissionId() == null) {
            throw new IllegalArgumentException("admissionId is required");
        }
        IpdAdmission a = assertAdmission(incoming.getAdmissionId());
        if (incoming.getIsolationType() == null || incoming.getIsolationType().isBlank()) {
            throw new IllegalArgumentException("isolationType is required");
        }
        InfectionIsolation row = new InfectionIsolation();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(incoming.getAdmissionId());
        row.setIsolationType(incoming.getIsolationType().trim().toUpperCase(Locale.ROOT));
        row.setReasonCode(incoming.getReasonCode() != null ? incoming.getReasonCode().trim().toUpperCase(Locale.ROOT) : null);
        row.setPathogen(incoming.getPathogen());
        row.setPpeRequired(incoming.getPpeRequired() != null ? incoming.getPpeRequired()
                : defaultPpe(row.getIsolationType()));
        row.setCleaningNotes(incoming.getCleaningNotes());
        row.setActive(true);
        row.setStartedAt(LocalDateTime.now());
        row.setCreatedBy(TenantContext.currentActor());
        InfectionIsolation saved = isolationRepository.save(row);
        if (a.getBedId() != null) {
            accommodationClient.setBedStatus(a.getBedId(), "ISOLATION");
        }
        return saved;
    }

    @Transactional
    public InfectionIsolation end(Long id) {
        InfectionIsolation row = isolationRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Isolation record not found"));
        row.setActive(false);
        row.setEndedAt(LocalDateTime.now());
        InfectionIsolation saved = isolationRepository.save(row);
        IpdAdmission a = assertAdmission(row.getAdmissionId());
        if (a.getBedId() != null && ("ADMITTED".equalsIgnoreCase(a.getStatus())
                || "TRANSFERRED".equalsIgnoreCase(a.getStatus()))) {
            accommodationClient.setBedStatus(a.getBedId(), "OCCUPIED");
        }
        return saved;
    }

    public List<HaiEvent> listHai(String status) {
        if (!haiEnabled) {
            return List.of();
        }
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        if (status != null && !status.isBlank()) {
            return haiEventRepository.findByTenantIdAndShopIdAndStatusOrderByCreatedAtDesc(
                    tenantId, shopId, status.trim().toUpperCase(Locale.ROOT));
        }
        return haiEventRepository.findByTenantIdAndShopIdOrderByCreatedAtDesc(tenantId, shopId);
    }

    @Transactional
    public HaiEvent reportHai(HaiEvent incoming) {
        if (!haiEnabled) {
            throw new IllegalStateException("HAI registry disabled");
        }
        if (incoming.getAdmissionId() == null) {
            throw new IllegalArgumentException("admissionId is required");
        }
        assertAdmission(incoming.getAdmissionId());
        if (incoming.getHaiType() == null || incoming.getHaiType().isBlank()) {
            throw new IllegalArgumentException("haiType is required (CLABSI|CAUTI|SSI|VAP|CDI|OTHER)");
        }
        HaiEvent row = new HaiEvent();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(incoming.getAdmissionId());
        row.setHaiType(incoming.getHaiType().trim().toUpperCase(Locale.ROOT));
        row.setOnsetDate(incoming.getOnsetDate());
        row.setDeviceType(incoming.getDeviceType());
        row.setIsolationId(incoming.getIsolationId());
        row.setNotes(incoming.getNotes());
        row.setStatus("OPEN");
        row.setCreatedBy(TenantContext.currentActor());
        return haiEventRepository.save(row);
    }

    @Transactional
    public HaiEvent closeHai(Long id) {
        HaiEvent row = haiEventRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("HAI event not found"));
        row.setStatus("CLOSED");
        row.setClosedAt(LocalDateTime.now());
        row.setClosedBy(TenantContext.currentActor());
        return haiEventRepository.save(row);
    }

    private static String defaultPpe(String type) {
        return switch (type) {
            case "AIRBORNE" -> "N95, gown, gloves, eye protection";
            case "DROPLET" -> "Surgical mask, gown, gloves, eye protection";
            case "CONTACT", "MRSA" -> "Gown, gloves";
            case "COVID" -> "N95, gown, gloves, eye protection";
            default -> "As per hospital protocol";
        };
    }

    private IpdAdmission assertAdmission(Long admissionId) {
        return admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }
}
