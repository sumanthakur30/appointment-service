package com.shopmanagement.ipdservice.diet;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class DietService {

    private final DietPlanRepository dietPlanRepository;
    private final IpdAdmissionRepository admissionRepository;

    public DietService(DietPlanRepository dietPlanRepository, IpdAdmissionRepository admissionRepository) {
        this.dietPlanRepository = dietPlanRepository;
        this.admissionRepository = admissionRepository;
    }

    @Transactional
    public DietPlan upsertActive(Long admissionId, DietPlan incoming) {
        admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));

        dietPlanRepository.findFirstByTenantIdAndShopIdAndAdmissionIdAndActiveTrueOrderByEffectiveFromDesc(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    dietPlanRepository.save(existing);
                });

        DietPlan p = new DietPlan();
        p.setTenantId(TenantContext.requireTenantId());
        p.setShopId(TenantContext.requireShopId());
        p.setAdmissionId(admissionId);
        p.setDietType(incoming.getDietType() == null || incoming.getDietType().isBlank()
                ? "REGULAR" : incoming.getDietType().trim().toUpperCase());
        p.setFluidRestrictionMl(incoming.getFluidRestrictionMl());
        p.setBreakfast(incoming.getBreakfast());
        p.setLunch(incoming.getLunch());
        p.setDinner(incoming.getDinner());
        p.setSpecialNotes(incoming.getSpecialNotes());
        p.setActive(true);
        p.setDietician(incoming.getDietician() != null ? incoming.getDietician() : TenantContext.currentActor());
        p.setEffectiveFrom(incoming.getEffectiveFrom() != null ? incoming.getEffectiveFrom() : LocalDate.now());
        return dietPlanRepository.save(p);
    }

    public DietPlan active(Long admissionId) {
        return dietPlanRepository.findFirstByTenantIdAndShopIdAndAdmissionIdAndActiveTrueOrderByEffectiveFromDesc(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId)
                .orElse(null);
    }

    public List<DietPlan> history(Long admissionId) {
        return dietPlanRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByEffectiveFromDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }
}
