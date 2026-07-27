package com.shopmanagement.ipdservice.fhir;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdFhirExportRepository extends JpaRepository<IpdFhirExport, Long> {
    List<IpdFhirExport> findByTenantIdAndShopIdOrderByExportedAtDesc(Long tenantId, String shopId);

    List<IpdFhirExport> findByTenantIdAndShopIdAndAdmissionIdOrderByExportedAtDesc(
            Long tenantId, String shopId, Long admissionId);
}
