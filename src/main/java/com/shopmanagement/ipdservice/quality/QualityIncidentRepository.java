package com.shopmanagement.ipdservice.quality;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityIncidentRepository extends JpaRepository<QualityIncident, Long> {
    List<QualityIncident> findByTenantIdAndShopIdOrderByReportedAtDesc(Long tenantId, String shopId);

    Optional<QualityIncident> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
