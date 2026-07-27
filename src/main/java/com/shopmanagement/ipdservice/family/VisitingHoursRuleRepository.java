package com.shopmanagement.ipdservice.family;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitingHoursRuleRepository extends JpaRepository<VisitingHoursRule, Long> {
    List<VisitingHoursRule> findByTenantIdAndShopIdAndActiveTrueOrderByStartTimeAsc(Long tenantId, String shopId);
}
