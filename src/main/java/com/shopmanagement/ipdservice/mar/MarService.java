package com.shopmanagement.ipdservice.mar;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.shopmanagement.ipdservice.clinical.PatientAllergy;
import com.shopmanagement.ipdservice.clinical.PatientClinicalProfileService;
import com.shopmanagement.ipdservice.pharmacy.PharmacyFloorService;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class MarService {

    private static final Set<String> ADMIN_STATUSES = Set.of("GIVEN", "MISSED", "DELAYED", "REFUSED", "HELD");
    private static final List<String> ACTIVE = List.of("ADMITTED", "TRANSFERRED");

    private final MarOrderRepository orderRepository;
    private final MarAdministrationRepository adminRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final MarStockClient marStockClient;
    private final PatientClinicalProfileService clinicalProfileService;
    private final PharmacyFloorService pharmacyFloorService;
    private final boolean barcodeRequired;
    private final boolean fiveRightsEnabled;
    private final boolean controlledDrugsEnabled;

    public MarService(
            MarOrderRepository orderRepository,
            MarAdministrationRepository adminRepository,
            IpdAdmissionRepository admissionRepository,
            MarStockClient marStockClient,
            PatientClinicalProfileService clinicalProfileService,
            PharmacyFloorService pharmacyFloorService,
            @Value("${ipd.mar.barcode-required:false}") boolean barcodeRequired,
            @Value("${ipd.mar.five-rights-enabled:true}") boolean fiveRightsEnabled,
            @Value("${ipd.controlled-drugs.enabled:true}") boolean controlledDrugsEnabled) {
        this.orderRepository = orderRepository;
        this.adminRepository = adminRepository;
        this.admissionRepository = admissionRepository;
        this.marStockClient = marStockClient;
        this.clinicalProfileService = clinicalProfileService;
        this.pharmacyFloorService = pharmacyFloorService;
        this.barcodeRequired = barcodeRequired;
        this.fiveRightsEnabled = fiveRightsEnabled;
        this.controlledDrugsEnabled = controlledDrugsEnabled;
    }

    @Transactional
    public MarOrder createOrder(Long admissionId, MarOrder incoming) {
        assertActive(admissionId);
        if (incoming.getMedicineName() == null || incoming.getMedicineName().isBlank()) {
            throw new IllegalArgumentException("medicineName is required");
        }
        MarOrder o = new MarOrder();
        o.setTenantId(TenantContext.requireTenantId());
        o.setShopId(TenantContext.requireShopId());
        o.setAdmissionId(admissionId);
        o.setMedicineName(incoming.getMedicineName().trim());
        o.setDose(incoming.getDose());
        o.setRoute(incoming.getRoute());
        o.setFrequency(incoming.getFrequency());
        o.setScheduleTimes(incoming.getScheduleTimes());
        o.setStartAt(incoming.getStartAt() != null ? incoming.getStartAt() : LocalDateTime.now());
        o.setEndAt(incoming.getEndAt());
        o.setStatus("ACTIVE");
        o.setOrderedBy(TenantContext.currentActor());
        o.setBarcode(incoming.getBarcode());
        o.setNotes(incoming.getNotes());
        o.setProductId(incoming.getProductId());
        Integer qty = incoming.getDispenseQuantity();
        if (qty != null && qty < 0) {
            throw new IllegalArgumentException("dispenseQuantity must be >= 0");
        }
        o.setDispenseQuantity(qty != null && qty > 0 ? qty : (incoming.getProductId() != null ? 1 : null));
        o.setControlledDrug(incoming.isControlledDrug());
        return orderRepository.save(o);
    }

    public List<MarOrder> listOrders(Long admissionId) {
        assertAdmission(admissionId);
        return orderRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByStartAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    @Transactional
    public MarOrder stopOrder(Long orderId) {
        MarOrder o = orderRepository.findByIdAndTenantIdAndShopId(
                        orderId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("MAR order not found"));
        o.setStatus("STOPPED");
        o.setEndAt(LocalDateTime.now());
        return orderRepository.save(o);
    }

    @Transactional
    public MarAdministration administer(Long orderId, MarAdministration incoming) {
        MarOrder o = orderRepository.findByIdAndTenantIdAndShopId(
                        orderId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("MAR order not found"));
        if (!"ACTIVE".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("Order is not active");
        }
        String status = incoming.getStatus() == null ? "GIVEN" : incoming.getStatus().trim().toUpperCase(Locale.ROOT);
        if (!ADMIN_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status; use " + ADMIN_STATUSES);
        }
        if (("MISSED".equals(status) || "DELAYED".equals(status) || "REFUSED".equals(status) || "HELD".equals(status))
                && (incoming.getReason() == null || incoming.getReason().isBlank())) {
            throw new IllegalArgumentException("reason is required for " + status);
        }

        String fiveRightsDetail = null;
        boolean fiveRightsOk = false;
        if ("GIVEN".equals(status) && fiveRightsEnabled) {
            FiveRightsResult rights = verifyFiveRights(o, incoming);
            fiveRightsDetail = rights.detail();
            fiveRightsOk = rights.ok();
            if (!rights.ok()) {
                throw new IllegalArgumentException("Five-rights check failed: " + rights.detail());
            }
        } else if (incoming.getBarcodeScanned() != null && o.getBarcode() != null
                && !o.getBarcode().isBlank()
                && !o.getBarcode().equalsIgnoreCase(incoming.getBarcodeScanned().trim())) {
            throw new IllegalArgumentException("Barcode mismatch");
        }

        String allergyMatch = null;
        if ("GIVEN".equals(status)) {
            allergyMatch = findAllergyConflict(o);
            if (allergyMatch != null) {
                if (!incoming.isAllergyOverride()) {
                    throw new IllegalStateException(
                            "Allergy hard-stop: medicine may conflict with patient allergy ["
                                    + allergyMatch + "]. Pass allergyOverride=true with reason to proceed.");
                }
                if (incoming.getReason() == null || incoming.getReason().isBlank()) {
                    throw new IllegalArgumentException("reason is required when overriding allergy hard-stop");
                }
            }
        }

        if ("GIVEN".equals(status) && controlledDrugsEnabled && o.isControlledDrug()) {
            if (incoming.getWitnessId() == null || incoming.getWitnessId().isBlank()) {
                throw new IllegalArgumentException("witnessId is required for controlled-drug administration");
            }
        }

        MarAdministration a = new MarAdministration();
        a.setTenantId(o.getTenantId());
        a.setShopId(o.getShopId());
        a.setMarOrderId(o.getId());
        a.setAdmissionId(o.getAdmissionId());
        a.setScheduledAt(incoming.getScheduledAt());
        a.setAdministeredAt(incoming.getAdministeredAt() != null ? incoming.getAdministeredAt() : LocalDateTime.now());
        a.setDoseGiven(incoming.getDoseGiven() != null ? incoming.getDoseGiven() : o.getDose());
        a.setStatus(status);
        a.setNurseId(TenantContext.currentActor());
        a.setReason(incoming.getReason());
        a.setBarcodeScanned(incoming.getBarcodeScanned());
        a.setStockStatus("NONE");
        a.setAllergyOverride(incoming.isAllergyOverride());
        a.setAllergyMatch(allergyMatch);
        a.setFiveRightsVerified(fiveRightsOk);
        a.setFiveRightsDetail(fiveRightsDetail);
        a.setPatientIdConfirmed(incoming.getPatientIdConfirmed());
        a.setWitnessId(incoming.getWitnessId());

        if ("GIVEN".equals(status) && o.getProductId() != null
                && o.getDispenseQuantity() != null && o.getDispenseQuantity() > 0) {
            if (!marStockClient.isEnabled()) {
                a.setStockStatus("SKIPPED");
                a.setStockDetail("Stock link disabled");
            } else {
                String key = "IPD-MAR-" + o.getId() + "-" + System.currentTimeMillis();
                a.setStockReservationKey(key);
                marStockClient.reserveAndCommit(
                        o.getProductId(),
                        o.getDispenseQuantity(),
                        key,
                        o.getId());
                a.setStockStatus("COMMITTED");
                a.setStockDetail("FEFO reserved+committed qty=" + o.getDispenseQuantity()
                        + " productId=" + o.getProductId());
            }
        }

        MarAdministration saved = adminRepository.save(a);
        if ("GIVEN".equals(status) && controlledDrugsEnabled && o.isControlledDrug()) {
            java.math.BigDecimal qty = o.getDispenseQuantity() != null
                    ? java.math.BigDecimal.valueOf(o.getDispenseQuantity())
                    : java.math.BigDecimal.ONE;
            pharmacyFloorService.recordMarAdmin(
                    o.getAdmissionId(),
                    o.getProductId(),
                    o.getMedicineName(),
                    qty,
                    saved.getNurseId(),
                    saved.getWitnessId(),
                    saved.getId());
        }
        return saved;
    }

    private FiveRightsResult verifyFiveRights(MarOrder order, MarAdministration incoming) {
        List<String> ok = new ArrayList<>();
        List<String> fail = new ArrayList<>();

        // 1. Right patient
        IpdAdmission admission = assertAdmission(order.getAdmissionId());
        String expectedPatient = String.valueOf(admission.getPatientId());
        String confirmed = incoming.getPatientIdConfirmed() == null ? "" : incoming.getPatientIdConfirmed().trim();
        if (confirmed.isEmpty()) {
            fail.add("patient (confirm patientId)");
        } else if (!expectedPatient.equals(confirmed)
                && !(admission.getAdmissionNo() != null
                        && admission.getAdmissionNo().equalsIgnoreCase(confirmed))) {
            fail.add("patient (expected " + expectedPatient + " or admission no)");
        } else {
            ok.add("patient");
        }

        // 2. Right drug (barcode when present / required)
        boolean orderHasBarcode = order.getBarcode() != null && !order.getBarcode().isBlank();
        String scanned = incoming.getBarcodeScanned() == null ? "" : incoming.getBarcodeScanned().trim();
        if (barcodeRequired || orderHasBarcode) {
            if (scanned.isEmpty()) {
                fail.add("drug barcode (scan required)");
            } else if (orderHasBarcode && !order.getBarcode().equalsIgnoreCase(scanned)) {
                fail.add("drug barcode mismatch");
            } else {
                ok.add("drug");
            }
        } else {
            ok.add("drug(no-barcode)");
        }

        // 3. Right dose
        String doseGiven = incoming.getDoseGiven() != null ? incoming.getDoseGiven().trim() : "";
        String orderedDose = order.getDose() == null ? "" : order.getDose().trim();
        if (!orderedDose.isEmpty() && !doseGiven.isEmpty()
                && !orderedDose.equalsIgnoreCase(doseGiven)) {
            fail.add("dose (ordered " + orderedDose + " vs given " + doseGiven + ")");
        } else {
            ok.add("dose");
        }

        // 4. Right route — order route must be present; nurse confirmation is order route unless overridden
        if (order.getRoute() != null && !order.getRoute().isBlank()) {
            ok.add("route(" + order.getRoute() + ")");
        } else {
            fail.add("route (missing on order)");
        }

        // 5. Right time — if scheduledAt provided, allow ±60 minutes
        LocalDateTime administeredAt =
                incoming.getAdministeredAt() != null ? incoming.getAdministeredAt() : LocalDateTime.now();
        if (incoming.getScheduledAt() != null) {
            long minutes = Math.abs(java.time.Duration.between(incoming.getScheduledAt(), administeredAt).toMinutes());
            if (minutes > 60) {
                fail.add("time (outside ±60m of schedule)");
            } else {
                ok.add("time");
            }
        } else {
            ok.add("time(unscheduled)");
        }

        String detail = "ok=[" + String.join(",", ok) + "]"
                + (fail.isEmpty() ? "" : " fail=[" + String.join("; ", fail) + "]");
        return new FiveRightsResult(fail.isEmpty(), detail);
    }

    private record FiveRightsResult(boolean ok, String detail) {}

    public List<MarAdministration> listAdministrations(Long admissionId) {
        assertAdmission(admissionId);
        return adminRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByAdministeredAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    /**
     * On discharge: stop active MAR orders and return committed pharmacy stock to inventory.
     */
    @Transactional
    public Map<String, Object> reconcileOnDischarge(Long admissionId) {
        IpdAdmission admission = assertAdmission(admissionId);
        List<MarOrder> orders = listOrders(admissionId);
        int stopped = 0;
        for (MarOrder o : orders) {
            if ("ACTIVE".equalsIgnoreCase(o.getStatus())) {
                o.setStatus("STOPPED");
                o.setEndAt(LocalDateTime.now());
                orderRepository.save(o);
                stopped++;
            }
        }

        List<MarAdministration> admins = listAdministrations(admissionId);
        int returnedLines = 0;
        int returnedQty = 0;
        List<String> details = new ArrayList<>();
        for (MarAdministration a : admins) {
            if (!"GIVEN".equalsIgnoreCase(a.getStatus())) {
                continue;
            }
            if (!"COMMITTED".equalsIgnoreCase(a.getStockStatus())) {
                continue;
            }
            MarOrder order = orderRepository.findByIdAndTenantIdAndShopId(
                            a.getMarOrderId(), TenantContext.requireTenantId(), TenantContext.requireShopId())
                    .orElse(null);
            if (order == null || order.getProductId() == null
                    || order.getDispenseQuantity() == null || order.getDispenseQuantity() <= 0) {
                continue;
            }
            try {
                if (marStockClient.isEnabled()) {
                    marStockClient.returnToStock(
                            order.getProductId(),
                            order.getDispenseQuantity(),
                            "IPD discharge return admission=" + admission.getAdmissionNo());
                }
                a.setStockStatus("RETURNED");
                a.setStockDetail((a.getStockDetail() == null ? "" : a.getStockDetail() + " | ")
                        + "Returned qty=" + order.getDispenseQuantity() + " on discharge");
                adminRepository.save(a);
                returnedLines++;
                returnedQty += order.getDispenseQuantity();
                details.add(order.getMedicineName() + " x" + order.getDispenseQuantity());
            } catch (Exception ex) {
                a.setStockDetail((a.getStockDetail() == null ? "" : a.getStockDetail() + " | ")
                        + "Return failed: " + ex.getMessage());
                adminRepository.save(a);
                details.add("FAIL " + order.getMedicineName() + ": " + ex.getMessage());
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("admissionId", admissionId);
        out.put("ordersStopped", stopped);
        out.put("stockReturnLines", returnedLines);
        out.put("stockReturnQty", returnedQty);
        out.put("details", details);
        return out;
    }

    private String findAllergyConflict(MarOrder order) {
        IpdAdmission admission = assertAdmission(order.getAdmissionId());
        List<PatientAllergy> allergies = clinicalProfileService.listAllergies(admission.getPatientId()).stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .toList();
        if (allergies.isEmpty()) {
            return null;
        }
        String med = normalize(order.getMedicineName());
        for (PatientAllergy allergy : allergies) {
            String substance = normalize(allergy.getSubstance());
            if (substance.isEmpty() || med.isEmpty()) {
                continue;
            }
            if (med.contains(substance) || substance.contains(med)) {
                return allergy.getSubstance()
                        + (allergy.getSeverity() != null ? " (" + allergy.getSeverity() + ")" : "");
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private IpdAdmission assertAdmission(Long admissionId) {
        return admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }

    private void assertActive(Long admissionId) {
        IpdAdmission a = assertAdmission(admissionId);
        if (!ACTIVE.contains(a.getStatus())) {
            throw new IllegalStateException("Admission is not active: " + a.getStatus());
        }
    }
}
