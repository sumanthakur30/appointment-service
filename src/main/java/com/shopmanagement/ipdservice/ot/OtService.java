package com.shopmanagement.ipdservice.ot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private final OtPreferenceCardRepository preferenceCardRepository;
    private final OtImplantUsageRepository implantUsageRepository;

    public OtService(
            OtBookingRepository bookingRepository,
            IpdAdmissionRepository admissionRepository,
            OtPreferenceCardRepository preferenceCardRepository,
            OtImplantUsageRepository implantUsageRepository) {
        this.bookingRepository = bookingRepository;
        this.admissionRepository = admissionRepository;
        this.preferenceCardRepository = preferenceCardRepository;
        this.implantUsageRepository = implantUsageRepository;
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

    public List<OtPreferenceCard> listPreferenceCards() {
        ensureDefaultPreferenceCard();
        return preferenceCardRepository.findByTenantIdAndShopIdAndActiveTrueOrderByProcedureNameAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    @Transactional
    public OtPreferenceCard upsertPreferenceCard(OtPreferenceCard incoming) {
        if (incoming.getCode() == null || incoming.getCode().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (incoming.getProcedureName() == null || incoming.getProcedureName().isBlank()) {
            throw new IllegalArgumentException("procedureName is required");
        }
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        OtPreferenceCard row = preferenceCardRepository
                .findByTenantIdAndShopIdAndCodeIgnoreCase(tenantId, shopId, incoming.getCode().trim())
                .orElseGet(OtPreferenceCard::new);
        row.setTenantId(tenantId);
        row.setShopId(shopId);
        row.setCode(incoming.getCode().trim().toUpperCase(Locale.ROOT));
        row.setSurgeonName(incoming.getSurgeonName());
        row.setProcedureCode(incoming.getProcedureCode());
        row.setProcedureName(incoming.getProcedureName().trim());
        row.setInstrumentsJson(incoming.getInstrumentsJson());
        row.setImplantsJson(incoming.getImplantsJson());
        row.setNotes(incoming.getNotes());
        row.setActive(true);
        return preferenceCardRepository.save(row);
    }

    @Transactional
    public Map<String, Object> applyPreferenceCard(Long bookingId, String cardCode) {
        OtBooking b = bookingRepository.findByIdAndTenantIdAndShopId(
                        bookingId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("OT booking not found"));
        OtPreferenceCard card = preferenceCardRepository
                .findByTenantIdAndShopIdAndCodeIgnoreCase(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), cardCode)
                .orElseThrow(() -> new IllegalArgumentException("Preference card not found"));
        String note = "Preference card " + card.getCode() + " applied"
                + (card.getInstrumentsJson() != null ? " · instruments " + card.getInstrumentsJson() : "");
        b.setPreopNotes((b.getPreopNotes() == null ? "" : b.getPreopNotes() + "\n") + note);
        if (b.getProcedureName() == null || b.getProcedureName().isBlank()) {
            b.setProcedureName(card.getProcedureName());
        }
        if ((b.getSurgeonName() == null || b.getSurgeonName().isBlank()) && card.getSurgeonName() != null) {
            b.setSurgeonName(card.getSurgeonName());
        }
        bookingRepository.save(b);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("booking", b);
        out.put("preferenceCard", card);
        return out;
    }

    @Transactional
    public OtImplantUsage recordImplant(Long bookingId, OtImplantUsage incoming) {
        bookingRepository.findByIdAndTenantIdAndShopId(
                        bookingId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("OT booking not found"));
        if (incoming.getImplantSku() == null || incoming.getImplantSku().isBlank()) {
            throw new IllegalArgumentException("implantSku is required");
        }
        OtImplantUsage row = new OtImplantUsage();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setOtBookingId(bookingId);
        row.setImplantSku(incoming.getImplantSku().trim());
        row.setImplantName(incoming.getImplantName());
        row.setLotNumber(incoming.getLotNumber());
        row.setQuantity(incoming.getQuantity() > 0 ? incoming.getQuantity() : 1);
        row.setLaterality(incoming.getLaterality());
        row.setRecordedAt(LocalDateTime.now());
        row.setRecordedBy(TenantContext.currentActor());
        return implantUsageRepository.save(row);
    }

    public List<OtImplantUsage> listImplants(Long bookingId) {
        bookingRepository.findByIdAndTenantIdAndShopId(
                        bookingId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("OT booking not found"));
        return implantUsageRepository.findByTenantIdAndShopIdAndOtBookingIdOrderByRecordedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), bookingId);
    }

    private void ensureDefaultPreferenceCard() {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        if (preferenceCardRepository.findByTenantIdAndShopIdAndCodeIgnoreCase(tenantId, shopId, "LAP_CHOLE").isPresent()) {
            return;
        }
        OtPreferenceCard card = new OtPreferenceCard();
        card.setTenantId(tenantId);
        card.setShopId(shopId);
        card.setCode("LAP_CHOLE");
        card.setProcedureCode("LC");
        card.setProcedureName("Laparoscopic cholecystectomy");
        card.setSurgeonName("General");
        card.setInstrumentsJson("[\"lap tray\",\"clip applier\",\"suction irrigation\"]");
        card.setImplantsJson("[{\"sku\":\"CLIP-TI\",\"name\":\"Titanium clip\"}]");
        card.setNotes("Default demo preference card");
        card.setActive(true);
        preferenceCardRepository.save(card);
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
