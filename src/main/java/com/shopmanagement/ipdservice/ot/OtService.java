package com.shopmanagement.ipdservice.ot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class OtService {

    private static final Set<String> STATUSES = Set.of(
            "REQUESTED", "BOOKED", "PRE_OP", "IN_PROGRESS", "RECOVERY", "COMPLETED", "CANCELLED");
    private static final List<String> ACTIVE_ADMISSION = List.of("ADMITTED", "TRANSFERRED");

    private final OtBookingRepository bookingRepository;
    private final IpdAdmissionRepository admissionRepository;

    public OtService(OtBookingRepository bookingRepository, IpdAdmissionRepository admissionRepository) {
        this.bookingRepository = bookingRepository;
        this.admissionRepository = admissionRepository;
    }

    public List<OtBooking> list() {
        return bookingRepository.findByTenantIdAndShopIdOrderByScheduledStartDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    public List<OtBooking> listForAdmission(Long admissionId) {
        assertAdmission(admissionId);
        return bookingRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByScheduledStartDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    @Transactional
    public OtBooking book(OtBooking incoming) {
        if (incoming.getAdmissionId() == null) {
            throw new IllegalArgumentException("admissionId is required");
        }
        assertActiveAdmission(incoming.getAdmissionId());
        if (incoming.getProcedureName() == null || incoming.getProcedureName().isBlank()) {
            throw new IllegalArgumentException("procedureName is required");
        }
        if (incoming.getScheduledStart() == null) {
            throw new IllegalArgumentException("scheduledStart is required");
        }
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        assertNoTheatreConflict(tenantId, shopId, incoming);
        OtBooking b = new OtBooking();
        b.setTenantId(tenantId);
        b.setShopId(shopId);
        b.setAdmissionId(incoming.getAdmissionId());
        b.setBookingNo(nextBookingNo(tenantId, shopId));
        b.setTheatreCode(incoming.getTheatreCode());
        b.setTheatreName(incoming.getTheatreName());
        b.setProcedureName(incoming.getProcedureName().trim());
        b.setSurgeonName(incoming.getSurgeonName());
        b.setAnaesthetist(incoming.getAnaesthetist());
        b.setScheduledStart(incoming.getScheduledStart());
        b.setScheduledEnd(incoming.getScheduledEnd());
        b.setStatus(incoming.getStatus() == null || incoming.getStatus().isBlank()
                ? "BOOKED" : normalizeStatus(incoming.getStatus()));
        b.setPreopNotes(incoming.getPreopNotes());
        b.setRecoveryBedId(incoming.getRecoveryBedId());
        b.setCreatedBy(TenantContext.currentActor());
        return bookingRepository.save(b);
    }

    @Transactional
    public OtBooking advance(Long id, String status, String postopNotes, Long recoveryBedId) {
        OtBooking b = bookingRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("OT booking not found"));
        b.setStatus(normalizeStatus(status));
        if (postopNotes != null && !postopNotes.isBlank()) {
            b.setPostopNotes(postopNotes.trim());
        }
        if (recoveryBedId != null) {
            b.setRecoveryBedId(recoveryBedId);
        }
        return bookingRepository.save(b);
    }

    private void assertNoTheatreConflict(Long tenantId, String shopId, OtBooking incoming) {
        String theatre = incoming.getTheatreCode();
        if (theatre == null || theatre.isBlank()) {
            return;
        }
        var start = incoming.getScheduledStart();
        var end = incoming.getScheduledEnd() != null ? incoming.getScheduledEnd() : start.plusHours(2);
        for (OtBooking existing : bookingRepository.findByTenantIdAndShopIdOrderByScheduledStartDesc(tenantId, shopId)) {
            if (existing.getStatus() != null && "CANCELLED".equalsIgnoreCase(existing.getStatus())) {
                continue;
            }
            if (existing.getTheatreCode() == null || !theatre.equalsIgnoreCase(existing.getTheatreCode())) {
                continue;
            }
            var eStart = existing.getScheduledStart();
            if (eStart == null) {
                continue;
            }
            var eEnd = existing.getScheduledEnd() != null ? existing.getScheduledEnd() : eStart.plusHours(2);
            boolean overlap = start.isBefore(eEnd) && end.isAfter(eStart);
            if (overlap) {
                throw new IllegalStateException(
                        "Theatre " + theatre + " conflict with " + existing.getBookingNo()
                                + " (" + existing.getProcedureName() + ")");
            }
        }
    }

    private String normalizeStatus(String status) {
        String s = status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(s)) {
            throw new IllegalArgumentException("Invalid OT status; use " + STATUSES);
        }
        return s;
    }

    private void assertAdmission(Long admissionId) {
        admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }

    private void assertActiveAdmission(Long admissionId) {
        var a = admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
        if (!ACTIVE_ADMISSION.contains(a.getStatus())) {
            throw new IllegalStateException("Admission is not active: " + a.getStatus());
        }
    }

    private String nextBookingNo(Long tenantId, String shopId) {
        String prefix = "OT-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        long seq = bookingRepository.countByTenantIdAndShopIdAndBookingNoStartingWith(tenantId, shopId, prefix) + 1;
        return prefix + String.format("%04d", seq);
    }
}
