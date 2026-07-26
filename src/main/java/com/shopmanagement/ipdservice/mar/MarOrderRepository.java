package com.shopmanagement.ipdservice.mar;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarOrderRepository extends JpaRepository<MarOrder, Long> {
    List<MarOrder> findByTenantIdAndShopIdAndAdmissionIdOrderByStartAtDesc(
            Long tenantId, String shopId, Long admissionId);

    Optional<MarOrder> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
