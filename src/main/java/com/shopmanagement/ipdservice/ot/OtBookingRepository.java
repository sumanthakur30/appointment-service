package com.shopmanagement.ipdservice.ot;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtBookingRepository extends JpaRepository<OtBooking, Long> {
    List<OtBooking> findByTenantIdAndShopIdOrderByScheduledStartDesc(Long tenantId, String shopId);

    List<OtBooking> findByTenantIdAndShopIdAndAdmissionIdOrderByScheduledStartDesc(
            Long tenantId, String shopId, Long admissionId);

    Optional<OtBooking> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);

    long countByTenantIdAndShopIdAndBookingNoStartingWith(Long tenantId, String shopId, String prefix);
}
