package com.shopmanagement.ipdservice.clinical;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientAllergyRepository extends JpaRepository<PatientAllergy, Long> {
    List<PatientAllergy> findByTenantIdAndShopIdAndPatientIdOrderByNotedAtDesc(
            Long tenantId, String shopId, Long patientId);

    java.util.Optional<PatientAllergy> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
