package com.shopmanagement.ipdservice.ot;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtImplantUsageRepository extends JpaRepository<OtImplantUsage, Long> {
    List<OtImplantUsage> findByTenantIdAndShopIdAndOtBookingIdOrderByRecordedAtDesc(
            Long tenantId, String shopId, Long otBookingId);
}
