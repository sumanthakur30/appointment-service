package com.shopmanagement.ipdservice.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class SettlementLedgerService {

    private static final Set<String> ENTRY_TYPES = Set.of(
            "DEPOSIT", "PATIENT_PAY", "TPA_APPROVED", "TPA_RECEIVED", "ADJUSTMENT", "REFUND");
    private static final Set<String> SETTLING_CREDITS = Set.of("DEPOSIT", "PATIENT_PAY", "TPA_RECEIVED");

    private final SettlementEntryRepository settlementRepository;
    private final IpdBillRepository billRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final SettlementPaymentSyncService settlementPaymentSyncService;
    private final boolean enabled;

    public SettlementLedgerService(
            SettlementEntryRepository settlementRepository,
            IpdBillRepository billRepository,
            IpdAdmissionRepository admissionRepository,
            SettlementPaymentSyncService settlementPaymentSyncService,
            @Value("${ipd.billing.settlement-enabled:true}") boolean enabled) {
        this.settlementRepository = settlementRepository;
        this.billRepository = billRepository;
        this.admissionRepository = admissionRepository;
        this.settlementPaymentSyncService = settlementPaymentSyncService;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Map<String, Object> summary(Long admissionId) {
        IpdAdmission admission = assertAdmission(admissionId);
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        List<SettlementEntry> entries = settlementRepository
                .findByTenantIdAndShopIdAndAdmissionIdOrderByPostedAtAsc(tenantId, shopId, admissionId);
        IpdBill finalBill = billRepository
                .findByTenantIdAndShopIdAndAdmissionIdAndBillType(tenantId, shopId, admissionId, "FINAL")
                .orElse(null);
        BigDecimal billDue = finalBill != null && finalBill.getAmountDue() != null
                ? finalBill.getAmountDue() : BigDecimal.ZERO;
        BigDecimal settled = BigDecimal.ZERO;
        BigDecimal tpaApprovedMemo = BigDecimal.ZERO;
        int synced = 0;
        int pendingSync = 0;
        for (SettlementEntry e : entries) {
            if (!"POSTED".equalsIgnoreCase(e.getStatus())) {
                continue;
            }
            BigDecimal amt = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;
            if ("TPA_APPROVED".equalsIgnoreCase(e.getEntryType())) {
                tpaApprovedMemo = tpaApprovedMemo.add(amt);
            } else if ("DEBIT".equalsIgnoreCase(e.getDirection())
                    || "REFUND".equalsIgnoreCase(e.getEntryType())) {
                settled = settled.subtract(amt);
            } else if (SETTLING_CREDITS.contains(e.getEntryType().toUpperCase(Locale.ROOT))) {
                settled = settled.add(amt);
            } else if ("ADJUSTMENT".equalsIgnoreCase(e.getEntryType())) {
                settled = "DEBIT".equalsIgnoreCase(e.getDirection())
                        ? settled.subtract(amt) : settled.add(amt);
            }
            String ss = e.getSyncStatus() != null ? e.getSyncStatus().toUpperCase(Locale.ROOT) : "PENDING";
            if ("SYNCED".equals(ss)) {
                synced++;
            } else if ("PENDING".equals(ss) || "FAILED".equals(ss)) {
                pendingSync++;
            }
        }
        settled = settled.setScale(2, RoundingMode.HALF_UP);
        BigDecimal balance = billDue.subtract(settled).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", enabled);
        out.put("paymentSyncEnabled", settlementPaymentSyncService.isEnabled());
        out.put("admissionId", admissionId);
        out.put("admissionNo", admission.getAdmissionNo());
        out.put("billId", finalBill != null ? finalBill.getId() : null);
        out.put("billAmountDue", billDue);
        out.put("settledAmount", settled);
        out.put("tpaApprovedMemo", tpaApprovedMemo.setScale(2, RoundingMode.HALF_UP));
        out.put("balanceDue", balance);
        out.put("fullySettled", finalBill != null && balance.compareTo(BigDecimal.ZERO) == 0);
        out.put("syncedCount", synced);
        out.put("pendingSyncCount", pendingSync);
        out.put("entries", entries.stream().map(this::toMap).toList());
        return out;
    }

    @Transactional
    public SettlementEntry post(Long admissionId, SettlementEntry incoming) {
        if (!enabled) {
            throw new IllegalStateException("Settlement ledger disabled");
        }
        assertAdmission(admissionId);
        if (incoming.getEntryType() == null || incoming.getEntryType().isBlank()) {
            throw new IllegalArgumentException("entryType is required");
        }
        String type = incoming.getEntryType().trim().toUpperCase(Locale.ROOT);
        if (!ENTRY_TYPES.contains(type)) {
            throw new IllegalArgumentException("Invalid entryType; use " + ENTRY_TYPES);
        }
        if (incoming.getAmount() == null || incoming.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        String direction = incoming.getDirection() == null || incoming.getDirection().isBlank()
                ? defaultDirection(type)
                : incoming.getDirection().trim().toUpperCase(Locale.ROOT);
        if (!"CREDIT".equals(direction) && !"DEBIT".equals(direction)) {
            throw new IllegalArgumentException("direction must be CREDIT or DEBIT");
        }

        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        Long billId = billRepository
                .findByTenantIdAndShopIdAndAdmissionIdAndBillType(tenantId, shopId, admissionId, "FINAL")
                .map(IpdBill::getId)
                .orElse(null);

        SettlementEntry row = new SettlementEntry();
        row.setTenantId(tenantId);
        row.setShopId(shopId);
        row.setAdmissionId(admissionId);
        row.setBillId(billId);
        row.setEntryType(type);
        row.setAmount(incoming.getAmount().setScale(2, RoundingMode.HALF_UP));
        row.setDirection(direction);
        row.setReferenceNo(incoming.getReferenceNo());
        row.setNotes(incoming.getNotes());
        row.setStatus("POSTED");
        row.setPostedAt(LocalDateTime.now());
        row.setPostedBy(TenantContext.currentActor());
        row.setSyncStatus("PENDING");
        SettlementEntry saved = settlementRepository.save(row);
        return settlementPaymentSyncService.syncEntry(saved);
    }

    /** After finalize: memo TPA approved amount if present (does not reduce cash balance). */
    @Transactional
    public void seedOnFinalize(Long admissionId, IpdBill bill) {
        if (!enabled || bill == null) {
            return;
        }
        IpdAdmission admission = assertAdmission(admissionId);
        if (admission.getTpaApprovedAmount() == null
                || admission.getTpaApprovedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        boolean already = settlementRepository
                .findByTenantIdAndShopIdAndAdmissionIdOrderByPostedAtAsc(tenantId, shopId, admissionId)
                .stream()
                .anyMatch(e -> "TPA_APPROVED".equalsIgnoreCase(e.getEntryType()));
        if (already) {
            return;
        }
        SettlementEntry memo = new SettlementEntry();
        memo.setTenantId(tenantId);
        memo.setShopId(shopId);
        memo.setAdmissionId(admissionId);
        memo.setBillId(bill.getId());
        memo.setEntryType("TPA_APPROVED");
        memo.setAmount(admission.getTpaApprovedAmount().setScale(2, RoundingMode.HALF_UP));
        memo.setDirection("CREDIT");
        memo.setReferenceNo(admission.getTpaPreauthRef());
        memo.setNotes("Auto-posted TPA approved amount on bill finalize (memo — await TPA_RECEIVED)");
        memo.setStatus("POSTED");
        memo.setPostedAt(LocalDateTime.now());
        memo.setPostedBy(TenantContext.currentActor());
        memo.setSyncStatus("SKIPPED");
        memo.setSyncedAt(LocalDateTime.now());
        settlementRepository.save(memo);
    }

    private static String defaultDirection(String type) {
        return "REFUND".equals(type) ? "DEBIT" : "CREDIT";
    }

    private IpdAdmission assertAdmission(Long admissionId) {
        return admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }

    private Map<String, Object> toMap(SettlementEntry e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("admissionId", e.getAdmissionId());
        m.put("billId", e.getBillId());
        m.put("entryType", e.getEntryType());
        m.put("amount", e.getAmount());
        m.put("direction", e.getDirection());
        m.put("referenceNo", e.getReferenceNo());
        m.put("notes", e.getNotes());
        m.put("status", e.getStatus());
        m.put("postedAt", e.getPostedAt());
        m.put("postedBy", e.getPostedBy());
        m.put("syncStatus", e.getSyncStatus());
        m.put("externalPaymentId", e.getExternalPaymentId());
        m.put("externalOrderIds", e.getExternalOrderIds());
        m.put("syncError", e.getSyncError());
        m.put("syncedAt", e.getSyncedAt());
        return m;
    }
}
