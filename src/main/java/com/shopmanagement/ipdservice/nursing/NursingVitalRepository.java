package com.shopmanagement.ipdservice.nursing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NursingVitalRepository extends JpaRepository<NursingVital, Long> {
    List<NursingVital> findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(
            Long tenantId, String shopId, Long admissionId);
}
