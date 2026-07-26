package com.shopmanagement.ipdservice.mar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class MarService {

    private static final Set<String> ADMIN_STATUSES = Set.of("GIVEN", "MISSED", "DELAYED", "REFUSED", "HELD");
    private static final List<String> ACTIVE = List.of("ADMITTED", "TRANSFERRED");

    private final MarOrderRepository orderRepository;
    private final MarAdministrationRepository adminRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final MarStockClient marStockClient;

    public MarService(
            MarOrderRepository orderRepository,
            MarAdministrationRepository adminRepository,
            IpdAdmissionRepository admissionRepository,
            MarStockClient marStockClient) {
        this.orderRepository = orderRepository;
        this.adminRepository = adminRepository;
        this.admissionRepository = admissionRepository;
        this.marStockClient = marStockClient;
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
        if (incoming.getBarcodeScanned() != null && o.getBarcode() != null
                && !o.getBarcode().isBlank()
                && !o.getBarcode().equalsIgnoreCase(incoming.getBarcodeScanned().trim())) {
            throw new IllegalArgumentException("Barcode mismatch");
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

        return adminRepository.save(a);
    }

    public List<MarAdministration> listAdministrations(Long admissionId) {
        assertAdmission(admissionId);
        return adminRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByAdministeredAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
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
