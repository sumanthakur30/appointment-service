package com.shopmanagement.ipdservice.billing;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.billing.OrderBillingClient.IpdSettlementPaymentRequest;
import com.shopmanagement.ipdservice.billing.OrderBillingClient.TenantHeaders;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.filter.RequestIdFilter;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class SettlementPaymentSyncService {

    private static final Logger log = LoggerFactory.getLogger(SettlementPaymentSyncService.class);
    private static final Set<String> SYNCABLE = Set.of("DEPOSIT", "PATIENT_PAY", "TPA_RECEIVED", "REFUND");

    private final SettlementEntryRepository settlementRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final OrderBillingClient orderBillingClient;

    public SettlementPaymentSyncService(
            SettlementEntryRepository settlementRepository,
            IpdAdmissionRepository admissionRepository,
            OrderBillingClient orderBillingClient) {
        this.settlementRepository = settlementRepository;
        this.admissionRepository = admissionRepository;
        this.orderBillingClient = orderBillingClient;
    }

    public boolean isEnabled() {
        return orderBillingClient.isSettlementSyncEnabled();
    }

    @Transactional
    public SettlementEntry syncEntry(SettlementEntry entry) {
        if (entry == null || entry.getId() == null) {
            return entry;
        }
        if (!orderBillingClient.isSettlementSyncEnabled()) {
            entry.setSyncStatus("SKIPPED");
            entry.setSyncError("settlement sync disabled");
            return settlementRepository.save(entry);
        }
        String type = entry.getEntryType() != null ? entry.getEntryType().toUpperCase(Locale.ROOT) : "";
        if (!SYNCABLE.contains(type) || "TPA_APPROVED".equals(type)) {
            entry.setSyncStatus("SKIPPED");
            entry.setSyncError(null);
            entry.setSyncedAt(LocalDateTime.now());
            return settlementRepository.save(entry);
        }
        IpdAdmission admission = admissionRepository
                .findByIdAndTenantIdAndShopId(entry.getAdmissionId(), entry.getTenantId(), entry.getShopId())
                .orElse(null);
        if (admission == null) {
            entry.setSyncStatus("FAILED");
            entry.setSyncError("admission not found");
            return settlementRepository.save(entry);
        }

        IpdSettlementPaymentRequest req = new IpdSettlementPaymentRequest();
        req.customerId = admission.getPatientId();
        req.encounterId = admission.getEncounterId();
        req.admissionId = admission.getId();
        req.admissionNo = admission.getAdmissionNo();
        req.settlementEntryId = entry.getId();
        req.entryType = entry.getEntryType();
        req.amount = entry.getAmount();
        req.referenceNo = entry.getReferenceNo();
        req.notes = entry.getNotes();
        req.direction = entry.getDirection();
        req.idempotencyKey = "IPD_SETTLE:" + entry.getTenantId() + ":" + entry.getShopId() + ":" + entry.getId();

        try {
            Map<String, Object> result = orderBillingClient.postSettlementPayment(req, resolveHeaders(admission));
            if (result == null) {
                entry.setSyncStatus("SKIPPED");
                entry.setSyncError("client disabled");
                return settlementRepository.save(entry);
            }
            Object paymentId = result.get("paymentId");
            if (paymentId instanceof Number n) {
                entry.setExternalPaymentId(n.longValue());
            }
            Object orderIds = result.get("orderIds");
            if (orderIds instanceof List<?> list) {
                entry.setExternalOrderIds(list.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
            entry.setSyncStatus("SYNCED");
            entry.setSyncError(null);
            entry.setSyncedAt(LocalDateTime.now());
            log.info("Settlement {} synced → paymentId={}", entry.getId(), entry.getExternalPaymentId());
            return settlementRepository.save(entry);
        } catch (Exception ex) {
            entry.setSyncStatus("FAILED");
            entry.setSyncError(ex.getMessage() != null ? ex.getMessage() : "sync failed");
            log.warn("Settlement {} sync failed: {}", entry.getId(), ex.getMessage());
            return settlementRepository.save(entry);
        }
    }

    @Transactional
    public Map<String, Object> syncPendingForCurrentTenant() {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        List<SettlementEntry> pending = settlementRepository
                .findByTenantIdAndShopIdAndSyncStatusInOrderByPostedAtAsc(
                        tenantId, shopId, List.of("PENDING", "FAILED"));
        int ok = 0;
        int fail = 0;
        int skipped = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        for (SettlementEntry e : pending) {
            SettlementEntry synced = syncEntry(e);
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("id", synced.getId());
            d.put("syncStatus", synced.getSyncStatus());
            d.put("externalPaymentId", synced.getExternalPaymentId());
            details.add(d);
            if ("SYNCED".equalsIgnoreCase(synced.getSyncStatus())) {
                ok++;
            } else if ("SKIPPED".equalsIgnoreCase(synced.getSyncStatus())) {
                skipped++;
            } else {
                fail++;
            }
        }
        return Map.of(
                "enabled", isEnabled(),
                "synced", ok,
                "failed", fail,
                "skipped", skipped,
                "details", details);
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
