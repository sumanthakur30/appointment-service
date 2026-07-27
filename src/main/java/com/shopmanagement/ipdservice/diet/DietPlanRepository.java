package com.shopmanagement.ipdservice.diet;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    List<DietPlan> findByTenantIdAndShopIdAndAdmissionIdOrderByEffectiveFromDesc(
            Long tenantId, String shopId, Long admissionId);

    List<DietPlan> findByTenantIdAndShopIdAndActiveTrueOrderByEffectiveFromDesc(Long tenantId, String shopId);

    Optional<DietPlan> findFirstByTenantIdAndShopIdAndAdmissionIdAndActiveTrueOrderByEffectiveFromDesc(
            Long tenantId, String shopId, Long admissionId);

    Optional<DietPlan> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
