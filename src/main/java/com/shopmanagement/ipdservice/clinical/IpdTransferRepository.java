package com.shopmanagement.ipdservice.clinical;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdTransferRepository extends JpaRepository<IpdTransfer, Long> {
    List<IpdTransfer> findByTenantIdAndShopIdAndAdmissionIdOrderByTransferredAtDesc(
            Long tenantId, String shopId, Long admissionId);
}
