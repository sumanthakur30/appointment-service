package com.shopmanagement.ipdservice.ot;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtPreferenceCardRepository extends JpaRepository<OtPreferenceCard, Long> {
    List<OtPreferenceCard> findByTenantIdAndShopIdAndActiveTrueOrderByProcedureNameAsc(Long tenantId, String shopId);

    Optional<OtPreferenceCard> findByTenantIdAndShopIdAndCodeIgnoreCase(Long tenantId, String shopId, String code);
}
