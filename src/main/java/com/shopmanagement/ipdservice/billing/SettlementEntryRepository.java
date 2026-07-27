package com.shopmanagement.ipdservice.billing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementEntryRepository extends JpaRepository<SettlementEntry, Long> {
    List<SettlementEntry> findByTenantIdAndShopIdAndAdmissionIdOrderByPostedAtAsc(
            Long tenantId, String shopId, Long admissionId);

    List<SettlementEntry> findByTenantIdAndShopIdAndSyncStatusInOrderByPostedAtAsc(
            Long tenantId, String shopId, List<String> syncStatuses);
}
