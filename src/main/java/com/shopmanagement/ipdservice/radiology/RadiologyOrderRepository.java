package com.shopmanagement.ipdservice.radiology;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RadiologyOrderRepository extends JpaRepository<RadiologyOrder, Long> {
    List<RadiologyOrder> findByTenantIdAndShopIdAndAdmissionIdOrderByOrderedAtDesc(
            Long tenantId, String shopId, Long admissionId);

    Optional<RadiologyOrder> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
