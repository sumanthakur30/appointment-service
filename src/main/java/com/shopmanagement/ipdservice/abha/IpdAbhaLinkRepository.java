package com.shopmanagement.ipdservice.abha;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdAbhaLinkRepository extends JpaRepository<IpdAbhaLink, Long> {
    Optional<IpdAbhaLink> findByTenantIdAndShopIdAndPatientIdAndActiveTrue(
            Long tenantId, String shopId, Long patientId);

    Optional<IpdAbhaLink> findByTenantIdAndShopIdAndPatientId(Long tenantId, String shopId, Long patientId);
}
