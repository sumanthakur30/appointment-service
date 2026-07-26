package com.shopmanagement.ipdservice.forms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdFormSubmissionRepository extends JpaRepository<IpdFormSubmission, Long> {
    List<IpdFormSubmission> findByTenantIdAndShopIdAndAdmissionIdOrderBySubmittedAtDesc(
            Long tenantId, String shopId, Long admissionId);

    List<IpdFormSubmission> findByTenantIdAndShopIdAndAdmissionIdAndPurposeOrderBySubmittedAtDesc(
            Long tenantId, String shopId, Long admissionId, String purpose);
}
