package com.shopmanagement.ipdservice.cssd;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class CssdService {

    private final CssdSetRepository setRepository;
    private final CssdCycleRepository cycleRepository;
    private final boolean enabled;

    public CssdService(
            CssdSetRepository setRepository,
            CssdCycleRepository cycleRepository,
            @Value("${ipd.cssd.enabled:true}") boolean enabled) {
        this.setRepository = setRepository;
        this.cycleRepository = cycleRepository;
        this.enabled = enabled;
    }

    public List<CssdSet> listSets() {
        requireEnabled();
        return setRepository.findByTenantIdAndShopIdOrderBySetCodeAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    public List<CssdCycle> listCycles() {
        requireEnabled();
        return cycleRepository.findByTenantIdAndShopIdOrderByStartedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    @Transactional
    public CssdSet registerSet(CssdSet incoming) {
        requireEnabled();
        if (incoming.getSetCode() == null || incoming.getSetCode().isBlank()
                || incoming.getSetName() == null || incoming.getSetName().isBlank()) {
            throw new IllegalArgumentException("setCode and setName are required");
        }
        CssdSet row = new CssdSet();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setSetCode(incoming.getSetCode().trim().toUpperCase(Locale.ROOT));
        row.setSetName(incoming.getSetName().trim());
        row.setSpecialty(incoming.getSpecialty());
        row.setStatus("AVAILABLE");
        row.setNotes(incoming.getNotes());
        return setRepository.save(row);
    }

    @Transactional
    public CssdCycle startCycle(Long setId, CssdCycle incoming) {
        requireEnabled();
        CssdSet set = requireSet(setId);
        set.setStatus("IN_STERILIZATION");
        setRepository.save(set);

        CssdCycle cycle = new CssdCycle();
        cycle.setTenantId(TenantContext.requireTenantId());
        cycle.setShopId(TenantContext.requireShopId());
        cycle.setSetId(setId);
        cycle.setCycleType(incoming != null && incoming.getCycleType() != null && !incoming.getCycleType().isBlank()
                ? incoming.getCycleType().trim().toUpperCase(Locale.ROOT) : "STEAM");
        cycle.setStatus("STARTED");
        cycle.setAutoclaveRef(incoming != null ? incoming.getAutoclaveRef() : null);
        cycle.setStartedAt(LocalDateTime.now());
        cycle.setPerformedBy(TenantContext.currentActor());
        cycle.setNotes(incoming != null ? incoming.getNotes() : null);
        return cycleRepository.save(cycle);
    }

    @Transactional
    public CssdCycle completeCycle(Long cycleId) {
        requireEnabled();
        CssdCycle cycle = cycleRepository.findById(cycleId)
                .filter(c -> TenantContext.requireTenantId().equals(c.getTenantId())
                        && TenantContext.requireShopId().equals(c.getShopId()))
                .orElseThrow(() -> new IllegalArgumentException("Cycle not found"));
        cycle.setStatus("COMPLETED");
        cycle.setCompletedAt(LocalDateTime.now());
        CssdSet set = requireSet(cycle.getSetId());
        set.setStatus("AVAILABLE");
        set.setLastSterilizedAt(LocalDateTime.now());
        set.setIssuedOtBookingId(null);
        setRepository.save(set);
        return cycleRepository.save(cycle);
    }

    @Transactional
    public CssdSet issueToOt(Long setId, Long otBookingId) {
        requireEnabled();
        if (otBookingId == null) {
            throw new IllegalArgumentException("otBookingId is required");
        }
        CssdSet set = requireSet(setId);
        if (!"AVAILABLE".equalsIgnoreCase(set.getStatus())) {
            throw new IllegalStateException("Set is not available (status=" + set.getStatus() + ")");
        }
        set.setStatus("ISSUED");
        set.setIssuedOtBookingId(otBookingId);
        return setRepository.save(set);
    }

    @Transactional
    public CssdSet returnFromOt(Long setId) {
        requireEnabled();
        CssdSet set = requireSet(setId);
        set.setStatus("DIRTY");
        set.setIssuedOtBookingId(null);
        return setRepository.save(set);
    }

    private CssdSet requireSet(Long setId) {
        return setRepository
                .findByIdAndTenantIdAndShopId(setId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("CSSD set not found"));
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new IllegalStateException("CSSD is disabled (ipd.cssd.enabled=false)");
        }
    }
}
