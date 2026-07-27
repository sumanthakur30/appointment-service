package com.shopmanagement.ipdservice.pharmacy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlledDrugRegisterRepository extends JpaRepository<ControlledDrugRegisterEntry, Long> {
    List<ControlledDrugRegisterEntry> findByTenantIdAndShopIdOrderByRecordedAtDesc(Long tenantId, String shopId);

    List<ControlledDrugRegisterEntry> findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(
            Long tenantId, String shopId, Long admissionId);
}
