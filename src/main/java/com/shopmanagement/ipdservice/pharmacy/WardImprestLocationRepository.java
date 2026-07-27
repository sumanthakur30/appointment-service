package com.shopmanagement.ipdservice.pharmacy;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WardImprestLocationRepository extends JpaRepository<WardImprestLocation, Long> {
    List<WardImprestLocation> findByTenantIdAndShopIdAndActiveTrueOrderByWardCodeAsc(Long tenantId, String shopId);

    Optional<WardImprestLocation> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
