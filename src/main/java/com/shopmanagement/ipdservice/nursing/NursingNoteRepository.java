package com.shopmanagement.ipdservice.nursing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NursingNoteRepository extends JpaRepository<NursingNote, Long> {
    List<NursingNote> findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(
            Long tenantId, String shopId, Long admissionId);

    List<NursingNote> findByTenantIdAndShopIdAndNoteTypeOrderByRecordedAtDesc(
            Long tenantId, String shopId, String noteType);
}
