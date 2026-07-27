package com.shopmanagement.ipdservice.infection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HaiEventRepository extends JpaRepository<HaiEvent, Long> {
    List<HaiEvent> findByTenantIdAndShopIdOrderByCreatedAtDesc(Long tenantId, String shopId);

    List<HaiEvent> findByTenantIdAndShopIdAndStatusOrderByCreatedAtDesc(Long tenantId, String shopId, String status);

    Optional<HaiEvent> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
