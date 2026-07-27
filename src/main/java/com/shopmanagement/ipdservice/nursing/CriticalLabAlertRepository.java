package com.shopmanagement.ipdservice.nursing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CriticalLabAlertRepository extends JpaRepository<CriticalLabAlert, Long> {
    List<CriticalLabAlert> findByTenantIdAndShopIdAndStatusOrderByCreatedAtDesc(
            Long tenantId, String shopId, String status);

    List<CriticalLabAlert> findByTenantIdAndShopIdAndAdmissionIdOrderByCreatedAtDesc(
            Long tenantId, String shopId, Long admissionId);

    Optional<CriticalLabAlert> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);

    Optional<CriticalLabAlert> findByTenantIdAndShopIdAndLabResultId(
            Long tenantId, String shopId, String labResultId);
}
