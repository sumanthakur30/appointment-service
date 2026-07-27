package com.shopmanagement.ipdservice.cpoe;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CpoeOrderSetRepository extends JpaRepository<CpoeOrderSet, Long> {
    List<CpoeOrderSet> findByTenantIdAndShopIdAndActiveTrueOrderByNameAsc(Long tenantId, String shopId);

    Optional<CpoeOrderSet> findByTenantIdAndShopIdAndCodeIgnoreCase(Long tenantId, String shopId, String code);
}
