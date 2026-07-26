package com.shopmanagement.ipdservice.infection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InfectionIsolationRepository extends JpaRepository<InfectionIsolation, Long> {
    List<InfectionIsolation> findByTenantIdAndShopIdAndActiveTrueOrderByStartedAtDesc(Long tenantId, String shopId);

    List<InfectionIsolation> findByTenantIdAndShopIdAndAdmissionIdOrderByStartedAtDesc(
            Long tenantId, String shopId, Long admissionId);

    Optional<InfectionIsolation> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
