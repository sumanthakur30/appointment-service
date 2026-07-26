package com.shopmanagement.ipdservice.mar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarAdministrationRepository extends JpaRepository<MarAdministration, Long> {
    List<MarAdministration> findByTenantIdAndShopIdAndAdmissionIdOrderByAdministeredAtDesc(
            Long tenantId, String shopId, Long admissionId);
}
