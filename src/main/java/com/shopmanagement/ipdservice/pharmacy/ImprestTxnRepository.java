package com.shopmanagement.ipdservice.pharmacy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImprestTxnRepository extends JpaRepository<ImprestTxn, Long> {
    List<ImprestTxn> findByTenantIdAndShopIdAndImprestLocationIdOrderByRecordedAtDesc(
            Long tenantId, String shopId, Long imprestLocationId);
}
