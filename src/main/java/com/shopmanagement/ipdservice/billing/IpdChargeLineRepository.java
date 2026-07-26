package com.shopmanagement.ipdservice.billing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IpdChargeLineRepository extends JpaRepository<IpdChargeLine, Long> {
    List<IpdChargeLine> findByTenantIdAndShopIdAndAdmissionIdOrderByChargeDateDesc(
            Long tenantId, String shopId, Long admissionId);

    Optional<IpdChargeLine> findByTenantIdAndShopIdAndAdmissionIdAndChargeDateAndChargeType(
            Long tenantId, String shopId, Long admissionId, LocalDate chargeDate, String chargeType);

    List<IpdChargeLine> findByTenantIdAndShopIdAndChargeDateOrderByAdmissionNoAsc(
            Long tenantId, String shopId, LocalDate chargeDate);

    List<IpdChargeLine> findByTenantIdAndShopIdAndAdmissionIdAndChargeDateAndStatus(
            Long tenantId, String shopId, Long admissionId, LocalDate chargeDate, String status);

    List<IpdChargeLine> findByStatus(String status);
}
