package com.shopmanagement.ipdservice.nursing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NursingIntakeOutputRepository extends JpaRepository<NursingIntakeOutput, Long> {
    List<NursingIntakeOutput> findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(
            Long tenantId, String shopId, Long admissionId);
}
