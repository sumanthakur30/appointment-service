package com.shopmanagement.ipdservice.cssd;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CssdCycleRepository extends JpaRepository<CssdCycle, Long> {
    List<CssdCycle> findByTenantIdAndShopIdOrderByStartedAtDesc(Long tenantId, String shopId);

    List<CssdCycle> findByTenantIdAndShopIdAndSetIdOrderByStartedAtDesc(
            Long tenantId, String shopId, Long setId);
}
