package com.shopmanagement.ipdservice.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class IpdBillService {

    private final IpdAdmissionRepository admissionRepository;
    private final IpdChargeLineRepository chargeRepository;
    private final IpdBillRepository billRepository;
    private final SettlementLedgerService settlementLedgerService;
    private final ObjectMapper objectMapper;

    public IpdBillService(
            IpdAdmissionRepository admissionRepository,
            IpdChargeLineRepository chargeRepository,
            IpdBillRepository billRepository,
            SettlementLedgerService settlementLedgerService,
            ObjectMapper objectMapper) {
        this.admissionRepository = admissionRepository;
        this.chargeRepository = chargeRepository;
        this.billRepository = billRepository;
        this.settlementLedgerService = settlementLedgerService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> interim(Long admissionId) {
        return buildStatement(admissionId, false);
    }

    public Map<String, Object> getFinal(Long admissionId) {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        return billRepository
                .findByTenantIdAndShopIdAndAdmissionIdAndBillType(tenantId, shopId, admissionId, "FINAL")
                .map(this::toMap)
                .orElseThrow(() -> new IllegalStateException("Final bill not found — finalize first"));
    }

    @Transactional
    public Map<String, Object> finalize(Long admissionId) {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        var existing = billRepository.findByTenantIdAndShopIdAndAdmissionIdAndBillType(
                tenantId, shopId, admissionId, "FINAL");
        if (existing.isPresent()) {
            return toMap(existing.get());
        }
        Map<String, Object> statement = buildStatement(admissionId, true);
        IpdBill bill = new IpdBill();
        bill.setTenantId(tenantId);
        bill.setShopId(shopId);
        bill.setAdmissionId(admissionId);
        bill.setAdmissionNo(String.valueOf(statement.get("admissionNo")));
        bill.setBillType("FINAL");
        bill.setPackageCode((String) statement.get("packageCode"));
        bill.setPackageAmount(asMoney(statement.get("packageAmount")));
        bill.setChargeTotal(asMoney(statement.get("chargeTotal")));
        bill.setDepositAmount(asMoney(statement.get("depositAmount")));
        bill.setGrossPayable(asMoney(statement.get("grossPayable")));
        bill.setAmountDue(asMoney(statement.get("amountDue")));
        bill.setStatus("FINALIZED");
        bill.setLinesJson(toJson(statement.get("lines")));
        bill.setFinalizedAt(LocalDateTime.now());
        bill.setFinalizedBy(TenantContext.currentActor());
        IpdBill saved = billRepository.save(bill);
        settlementLedgerService.seedOnFinalize(admissionId, saved);
        return toMap(saved);
    }

    private Map<String, Object> buildStatement(Long admissionId, boolean finalizing) {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        IpdAdmission admission = admissionRepository.findByIdAndTenantIdAndShopId(admissionId, tenantId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));

        List<IpdChargeLine> charges = chargeRepository
                .findByTenantIdAndShopIdAndAdmissionIdOrderByChargeDateDesc(tenantId, shopId, admissionId)
                .stream()
                .filter(c -> !"VOID".equalsIgnoreCase(c.getStatus()))
                .toList();

        BigDecimal chargeTotal = charges.stream()
                .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal packageAmount = admission.getPackageAmount() != null
                ? admission.getPackageAmount() : BigDecimal.ZERO;
        BigDecimal deposit = admission.getDepositAmount() != null
                ? admission.getDepositAmount() : BigDecimal.ZERO;
        // Package-lite: when a package amount is set, it becomes the gross payable; else sum of charges.
        BigDecimal gross = packageAmount.compareTo(BigDecimal.ZERO) > 0 ? packageAmount : chargeTotal;
        BigDecimal due = gross.subtract(deposit).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        List<Map<String, Object>> lines = new ArrayList<>();
        for (IpdChargeLine c : charges) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("id", c.getId());
            line.put("chargeDate", c.getChargeDate());
            line.put("chargeType", c.getChargeType());
            line.put("description", c.getDescription());
            line.put("amount", c.getAmount());
            line.put("status", c.getStatus());
            lines.add(line);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("admissionId", admission.getId());
        out.put("admissionNo", admission.getAdmissionNo());
        out.put("billType", finalizing ? "FINAL" : "INTERIM");
        out.put("packageCode", admission.getPackageCode());
        out.put("packageAmount", packageAmount);
        out.put("chargeTotal", chargeTotal);
        out.put("depositAmount", deposit);
        out.put("grossPayable", gross);
        out.put("amountDue", due);
        out.put("packageApplied", packageAmount.compareTo(BigDecimal.ZERO) > 0);
        out.put("lines", lines);
        out.put("status", finalizing ? "FINALIZED" : "DRAFT");
        return out;
    }

    private Map<String, Object> toMap(IpdBill bill) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", bill.getId());
        out.put("admissionId", bill.getAdmissionId());
        out.put("admissionNo", bill.getAdmissionNo());
        out.put("billType", bill.getBillType());
        out.put("packageCode", bill.getPackageCode());
        out.put("packageAmount", bill.getPackageAmount());
        out.put("chargeTotal", bill.getChargeTotal());
        out.put("depositAmount", bill.getDepositAmount());
        out.put("grossPayable", bill.getGrossPayable());
        out.put("amountDue", bill.getAmountDue());
        out.put("packageApplied",
                bill.getPackageAmount() != null && bill.getPackageAmount().compareTo(BigDecimal.ZERO) > 0);
        out.put("status", bill.getStatus());
        out.put("finalizedAt", bill.getFinalizedAt());
        out.put("finalizedBy", bill.getFinalizedBy());
        out.put("linesJson", bill.getLinesJson());
        return out;
    }

    private static BigDecimal asMoney(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }
}
