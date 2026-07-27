package com.shopmanagement.ipdservice.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.billing.ChargeSyncService;
import com.shopmanagement.ipdservice.billing.DailyBillingService;
import com.shopmanagement.ipdservice.billing.IpdBillService;
import com.shopmanagement.ipdservice.billing.IpdChargeLine;
import com.shopmanagement.ipdservice.billing.SettlementEntry;
import com.shopmanagement.ipdservice.billing.SettlementLedgerService;
import com.shopmanagement.ipdservice.billing.SettlementPaymentSyncService;

@RestController
@RequestMapping("/ipd/billing")
public class BillingController {

    private final DailyBillingService dailyBillingService;
    private final ChargeSyncService chargeSyncService;
    private final IpdBillService billService;
    private final SettlementLedgerService settlementLedgerService;
    private final SettlementPaymentSyncService settlementPaymentSyncService;

    public BillingController(
            DailyBillingService dailyBillingService,
            ChargeSyncService chargeSyncService,
            IpdBillService billService,
            SettlementLedgerService settlementLedgerService,
            SettlementPaymentSyncService settlementPaymentSyncService) {
        this.dailyBillingService = dailyBillingService;
        this.chargeSyncService = chargeSyncService;
        this.billService = billService;
        this.settlementLedgerService = settlementLedgerService;
        this.settlementPaymentSyncService = settlementPaymentSyncService;
    }

    @PostMapping("/run-daily")
    public Map<String, Object> runDaily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // Manual UI run bills today; midnight scheduler passes yesterday explicitly.
        LocalDate chargeDate = date != null ? date : LocalDate.now();
        return dailyBillingService.runForCurrentTenant(chargeDate);
    }

    /** Push POSTED charge lines to order-service (billType=IPD). */
    @PostMapping("/sync")
    public Map<String, Object> syncPending() {
        return chargeSyncService.syncPendingForCurrentTenant();
    }

    /** Retry PENDING/FAILED settlement → order-service payment sync. */
    @PostMapping("/settlements/sync")
    public Map<String, Object> syncSettlements() {
        return settlementPaymentSyncService.syncPendingForCurrentTenant();
    }

    @GetMapping("/admissions/{admissionId}/charges")
    public List<IpdChargeLine> charges(@PathVariable Long admissionId) {
        return dailyBillingService.listForAdmission(admissionId);
    }

    @GetMapping("/admissions/{admissionId}/interim")
    public Map<String, Object> interim(@PathVariable Long admissionId) {
        return billService.interim(admissionId);
    }

    @PostMapping("/admissions/{admissionId}/finalize")
    public Map<String, Object> finalizeBill(@PathVariable Long admissionId) {
        return billService.finalize(admissionId);
    }

    @GetMapping("/admissions/{admissionId}/final")
    public Map<String, Object> finalBill(@PathVariable Long admissionId) {
        return billService.getFinal(admissionId);
    }

    @GetMapping("/admissions/{admissionId}/settlements")
    public Map<String, Object> settlements(@PathVariable Long admissionId) {
        return settlementLedgerService.summary(admissionId);
    }

    @PostMapping("/admissions/{admissionId}/settlements")
    public SettlementEntry postSettlement(@PathVariable Long admissionId, @RequestBody SettlementEntry body) {
        return settlementLedgerService.post(admissionId, body);
    }

    @GetMapping("/by-date")
    public List<IpdChargeLine> byDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dailyBillingService.listForDate(date);
    }
}
