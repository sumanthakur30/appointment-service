package com.shopmanagement.ipdservice.quality;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityIndicatorDefRepository extends JpaRepository<QualityIndicatorDef, Long> {
    List<QualityIndicatorDef> findByTenantIdAndShopIdAndActiveTrueOrderBySortOrderAsc(Long tenantId, String shopId);

    Optional<QualityIndicatorDef> findByTenantIdAndShopIdAndCodeIgnoreCase(Long tenantId, String shopId, String code);
}
