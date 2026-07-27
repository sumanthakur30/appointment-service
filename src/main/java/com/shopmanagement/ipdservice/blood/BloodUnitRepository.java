package com.shopmanagement.ipdservice.blood;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodUnitRepository extends JpaRepository<BloodUnit, Long> {
    List<BloodUnit> findByTenantIdAndShopIdOrderByExpiresAtAsc(Long tenantId, String shopId);

    Optional<BloodUnit> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);

    Optional<BloodUnit> findFirstByTenantIdAndShopIdAndBloodGroupAndComponentAndStatusOrderByExpiresAtAsc(
            Long tenantId, String shopId, String bloodGroup, String component, String status);
}
