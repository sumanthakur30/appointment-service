package com.shopmanagement.ipdservice.blood;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByTenantIdAndShopIdOrderByRequestedAtDesc(Long tenantId, String shopId);

    List<BloodRequest> findByTenantIdAndShopIdAndAdmissionIdOrderByRequestedAtDesc(
            Long tenantId, String shopId, Long admissionId);

    Optional<BloodRequest> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
