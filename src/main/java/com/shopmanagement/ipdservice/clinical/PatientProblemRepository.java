package com.shopmanagement.ipdservice.clinical;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientProblemRepository extends JpaRepository<PatientProblem, Long> {
    List<PatientProblem> findByTenantIdAndShopIdAndPatientIdOrderByIdDesc(
            Long tenantId, String shopId, Long patientId);

    Optional<PatientProblem> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
