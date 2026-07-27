package com.shopmanagement.ipdservice.pharmacy;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImprestParLevelRepository extends JpaRepository<ImprestParLevel, Long> {
    List<ImprestParLevel> findByTenantIdAndShopIdAndImprestLocationIdOrderByMedicineNameAsc(
            Long tenantId, String shopId, Long imprestLocationId);

    Optional<ImprestParLevel> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
