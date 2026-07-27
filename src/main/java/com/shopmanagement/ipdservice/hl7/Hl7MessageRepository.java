package com.shopmanagement.ipdservice.hl7;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Hl7MessageRepository extends JpaRepository<Hl7Message, Long> {
    List<Hl7Message> findByTenantIdAndShopIdOrderByCreatedAtDesc(Long tenantId, String shopId);
}
