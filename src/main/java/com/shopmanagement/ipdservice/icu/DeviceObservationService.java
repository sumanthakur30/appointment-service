package com.shopmanagement.ipdservice.icu;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class DeviceObservationService {

    private final DeviceObservationRepository repository;
    private final IpdAdmissionRepository admissionRepository;
    private final boolean enabled;

    public DeviceObservationService(
            DeviceObservationRepository repository,
            IpdAdmissionRepository admissionRepository,
            @Value("${ipd.icu.device-panel-enabled:true}") boolean enabled) {
        this.repository = repository;
        this.admissionRepository = admissionRepository;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<DeviceObservation> list(Long admissionId) {
        assertAdmission(admissionId);
        return repository.findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    @Transactional
    public DeviceObservation record(Long admissionId, DeviceObservation incoming) {
        if (!enabled) {
            throw new IllegalStateException("ICU device panel disabled");
        }
        assertAdmission(admissionId);
        if (incoming.getDeviceType() == null || incoming.getDeviceType().isBlank()) {
            throw new IllegalArgumentException("deviceType is required");
        }
        DeviceObservation row = new DeviceObservation();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(admissionId);
        row.setDeviceType(incoming.getDeviceType().trim().toUpperCase(Locale.ROOT));
        row.setMode(incoming.getMode());
        row.setFio2(incoming.getFio2());
        row.setPeep(incoming.getPeep());
        row.setTidalVol(incoming.getTidalVol());
        row.setRate(incoming.getRate());
        row.setNotes(incoming.getNotes());
        row.setRecordedAt(LocalDateTime.now());
        row.setRecordedBy(TenantContext.currentActor());
        return repository.save(row);
    }

    private void assertAdmission(Long admissionId) {
        admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }
}
