package com.shopmanagement.ipdservice.billing;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdBillRepository extends JpaRepository<IpdBill, Long> {
    Optional<IpdBill> findByTenantIdAndShopIdAndAdmissionIdAndBillType(
            Long tenantId, String shopId, Long admissionId, String billType);
}
