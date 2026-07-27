package com.shopmanagement.ipdservice.icu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceObservationRepository extends JpaRepository<DeviceObservation, Long> {
    List<DeviceObservation> findByTenantIdAndShopIdAndAdmissionIdOrderByRecordedAtDesc(
            Long tenantId, String shopId, Long admissionId);
}
