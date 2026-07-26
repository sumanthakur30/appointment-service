package com.shopmanagement.ipdservice.family;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorPassRepository extends JpaRepository<VisitorPass, Long> {
    List<VisitorPass> findByTenantIdAndShopIdAndAdmissionIdOrderByCreatedAtDesc(
            Long tenantId, String shopId, Long admissionId);

    Optional<VisitorPass> findByTenantIdAndShopIdAndPassCode(Long tenantId, String shopId, String passCode);

    Optional<VisitorPass> findFirstByPassCodeIgnoreCase(String passCode);

    Optional<VisitorPass> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
