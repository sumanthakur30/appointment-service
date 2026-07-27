package com.shopmanagement.ipdservice.billing;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.billing.OrderBillingClient.IpdChargeSyncRequest;
import com.shopmanagement.ipdservice.billing.OrderBillingClient.TenantHeaders;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.filter.RequestIdFilter;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class ChargeSyncService {

    private static final Logger log = LoggerFactory.getLogger(ChargeSyncService.class);

    private final IpdChargeLineRepository chargeRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final OrderBillingClient orderBillingClient;

    public ChargeSyncService(
            IpdChargeLineRepository chargeRepository,
            IpdAdmissionRepository admissionRepository,
            OrderBillingClient orderBillingClient) {
        this.chargeRepository = chargeRepository;
        this.admissionRepository = admissionRepository;
        this.orderBillingClient = orderBillingClient;
    }

    @Transactional
    public Map<String, Object> syncAdmissionDay(IpdAdmission admission, LocalDate chargeDate) {
        if (!orderBillingClient.isEnabled()) {
            return Map.of("synced", false, "reason", "disabled");
        }
        List<IpdChargeLine> posted = chargeRepository
                .findByTenantIdAndShopIdAndAdmissionIdAndChargeDateAndStatus(
                        admission.getTenantId(), admission.getShopId(), admission.getId(), chargeDate, "POSTED");
        if (posted.isEmpty()) {
            return Map.of("synced", false, "reason", "nothing_to_sync", "admissionId", admission.getId());
        }

        IpdChargeSyncRequest req = new IpdChargeSyncRequest();
        req.customerId = admission.getPatientId();
        req.encounterId = admission.getEncounterId();
        req.admissionId = admission.getId();
        req.admissionNo = admission.getAdmissionNo();
        req.chargeDate = chargeDate;
        req.idempotencyKey = "IPD_DAILY:" + admission.getId() + ":" + chargeDate;
        for (IpdChargeLine line : posted) {
            IpdChargeSyncRequest.Line l = new IpdChargeSyncRequest.Line();
            l.chargeCode = line.getChargeType();
            l.productName = line.getDescription() != null ? line.getDescription() : line.getChargeType();
            l.quantity = line.getQuantity() != null ? line.getQuantity().doubleValue() : 1.0;
            l.amount = line.getUnitAmount() != null ? line.getUnitAmount() : line.getAmount();
            req.lines.add(l);
        }

        TenantHeaders headers = resolveHeaders(admission);
        Long orderId;
        try {
            orderId = orderBillingClient.postIpdCharges(req, headers);
        } catch (Exception ex) {
            log.warn("Sync group failed admission={} date={}: {}",
                    admission.getId(), chargeDate, ex.getMessage());
            return Map.of(
                    "synced", false,
                    "reason", ex.getMessage() != null ? ex.getMessage() : "sync_failed",
                    "admissionId", admission.getId(),
                    "chargeDate", chargeDate.toString());
        }
        if (orderId == null) {
            return Map.of("synced", false, "reason", "no_order_id", "admissionId", admission.getId());
        }
        String ref = String.valueOf(orderId);
        for (IpdChargeLine line : posted) {
            line.setStatus("SYNCED");
            line.setExternalRef(ref);
            chargeRepository.save(line);
        }
        log.info("Synced IPD charges admission={} date={} → orderId={}", admission.getId(), chargeDate, orderId);
        return Map.of(
                "synced", true,
                "admissionId", admission.getId(),
                "chargeDate", chargeDate.toString(),
                "orderId", orderId,
                "lines", posted.size());
    }

    /** Tenant-scoped: sync all POSTED lines for current shop (grouped by admission+date). */
    @Transactional
    public Map<String, Object> syncPendingForCurrentTenant() {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        List<IpdChargeLine> pending = chargeRepository.findByStatus("POSTED").stream()
                .filter(c -> tenantId.equals(c.getTenantId()) && shopId.equals(c.getShopId()))
                .toList();
        return syncGroups(pending);
    }

    /** Scheduler: sync all POSTED lines across tenants. */
    @Transactional
    public Map<String, Object> syncAllPending() {
        return syncGroups(chargeRepository.findByStatus("POSTED"));
    }

    private Map<String, Object> syncGroups(List<IpdChargeLine> pending) {
        if (!orderBillingClient.isEnabled()) {
            return Map.of("syncedGroups", 0, "reason", "disabled");
        }
        Map<String, List<IpdChargeLine>> groups = pending.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getAdmissionId() + "|" + c.getChargeDate(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        int ok = 0;
        int fail = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        for (List<IpdChargeLine> group : groups.values()) {
            if (group.isEmpty()) {
                continue;
            }
            IpdChargeLine sample = group.get(0);
            try {
                IpdAdmission admission = admissionRepository
                        .findByIdAndTenantIdAndShopId(sample.getAdmissionId(), sample.getTenantId(), sample.getShopId())
                        .orElse(null);
                if (admission == null) {
                    fail++;
                    continue;
                }
                Map<String, Object> result = syncAdmissionDay(admission, sample.getChargeDate());
                details.add(result);
                if (Boolean.TRUE.equals(result.get("synced"))) {
                    ok++;
                } else {
                    fail++;
                }
            } catch (Exception ex) {
                fail++;
                log.warn("Sync group failed admission={} date={}: {}",
                        sample.getAdmissionId(), sample.getChargeDate(), ex.getMessage());
            }
        }
        return Map.of("syncedGroups", ok, "failedGroups", fail, "details", details);
    }

    private TenantHeaders resolveHeaders(IpdAdmission admission) {
        Long tenantId = RequestIdFilter.getCurrentTenantId() != null
                ? RequestIdFilter.getCurrentTenantId()
                : admission.getTenantId();
        String shopId = RequestIdFilter.getCurrentShopId() != null
                ? RequestIdFilter.getCurrentShopId()
                : admission.getShopId();
        String role = RequestIdFilter.getCurrentRole() != null ? RequestIdFilter.getCurrentRole() : "SHOP_OWNER";
        String user = RequestIdFilter.getCurrentUser() != null ? RequestIdFilter.getCurrentUser() : "ipd-service";
        return new TenantHeaders(tenantId, shopId, role, user);
    }
}
