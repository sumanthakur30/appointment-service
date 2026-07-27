package com.shopmanagement.ipdservice.cssd;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CssdSetRepository extends JpaRepository<CssdSet, Long> {
    List<CssdSet> findByTenantIdAndShopIdOrderBySetCodeAsc(Long tenantId, String shopId);

    Optional<CssdSet> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
