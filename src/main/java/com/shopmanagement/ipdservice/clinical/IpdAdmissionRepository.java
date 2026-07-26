package com.shopmanagement.ipdservice.clinical;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdAdmissionRepository extends JpaRepository<IpdAdmission, Long> {
    List<IpdAdmission> findByTenantIdAndShopIdOrderByCreatedAtDesc(Long tenantId, String shopId);

    Optional<IpdAdmission> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);

    Optional<IpdAdmission> findByTenantIdAndShopIdAndEncounterId(Long tenantId, String shopId, Long encounterId);

    long countByTenantIdAndShopIdAndAdmissionNoStartingWith(Long tenantId, String shopId, String prefix);

    List<IpdAdmission> findByTenantIdAndShopIdAndStatusInOrderByAdmittedAtDesc(
            Long tenantId, String shopId, List<String> statuses);

    /** Scheduler / batch — all tenants. */
    List<IpdAdmission> findByStatusIn(List<String> statuses);
}
