package com.shopmanagement.ipdservice.er;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ErTriageRepository extends JpaRepository<ErTriage, Long> {
    List<ErTriage> findByTenantIdAndShopIdAndStatusInOrderByArrivalAtAsc(
            Long tenantId, String shopId, List<String> statuses);

    Optional<ErTriage> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
