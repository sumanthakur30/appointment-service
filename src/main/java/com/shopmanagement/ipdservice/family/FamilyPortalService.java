package com.shopmanagement.ipdservice.family;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.accommodation.client.AccommodationClient;
import com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationBedDto;
import com.shopmanagement.ipdservice.billing.IpdChargeLine;
import com.shopmanagement.ipdservice.billing.IpdChargeLineRepository;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class FamilyPortalService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final VisitorPassRepository passRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final AccommodationClient accommodationClient;
    private final IpdChargeLineRepository chargeRepository;
    private final FamilyPassQrService qrService;

    public FamilyPortalService(
            VisitorPassRepository passRepository,
            IpdAdmissionRepository admissionRepository,
            AccommodationClient accommodationClient,
            IpdChargeLineRepository chargeRepository,
            FamilyPassQrService qrService) {
        this.passRepository = passRepository;
        this.admissionRepository = admissionRepository;
        this.accommodationClient = accommodationClient;
        this.chargeRepository = chargeRepository;
        this.qrService = qrService;
    }

    public List<VisitorPass> listForAdmission(Long admissionId) {
        assertAdmission(admissionId);
        return passRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByCreatedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    @Transactional
    public VisitorPass issue(VisitorPass incoming) {
        if (incoming.getAdmissionId() == null) {
            throw new IllegalArgumentException("admissionId is required");
        }
        assertAdmission(incoming.getAdmissionId());
        if (incoming.getVisitorName() == null || incoming.getVisitorName().isBlank()) {
            throw new IllegalArgumentException("visitorName is required");
        }
        VisitorPass p = new VisitorPass();
        p.setTenantId(TenantContext.requireTenantId());
        p.setShopId(TenantContext.requireShopId());
        p.setAdmissionId(incoming.getAdmissionId());
        p.setPassCode(incoming.getPassCode() != null && !incoming.getPassCode().isBlank()
                ? incoming.getPassCode().trim().toUpperCase()
                : generatePassCode());
        p.setVisitorName(incoming.getVisitorName().trim());
        p.setRelation(incoming.getRelation());
        p.setPhone(incoming.getPhone());
        p.setVisitingHours(incoming.getVisitingHours() != null ? incoming.getVisitingHours() : "16:00–18:00");
        p.setValidFrom(incoming.getValidFrom() != null ? incoming.getValidFrom() : LocalDateTime.now());
        p.setValidTo(incoming.getValidTo());
        p.setStatus("ACTIVE");
        p.setCreatedBy(TenantContext.currentActor());
        return passRepository.save(p);
    }

    @Transactional
    public VisitorPass revoke(Long id) {
        VisitorPass p = passRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Visitor pass not found"));
        p.setStatus("REVOKED");
        return passRepository.save(p);
    }

    /** Staff lookup (tenant-scoped). */
    public Map<String, Object> lookupByPass(String passCode) {
        if (passCode == null || passCode.isBlank()) {
            throw new IllegalArgumentException("passCode is required");
        }
        VisitorPass pass = passRepository.findByTenantIdAndShopIdAndPassCode(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), passCode.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Pass not found"));
        return buildDashboard(pass);
    }

    /** Public family portal lookup by opaque pass code (no tenant header required). */
    public Map<String, Object> publicLookupByPass(String passCode) {
        if (passCode == null || passCode.isBlank()) {
            throw new IllegalArgumentException("passCode is required");
        }
        VisitorPass pass = passRepository.findFirstByPassCodeIgnoreCase(passCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Pass not found"));
        return buildDashboard(pass);
    }

    public Map<String, Object> passShareInfo(Long passId) {
        VisitorPass pass = passRepository.findByIdAndTenantIdAndShopId(
                        passId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Visitor pass not found"));
        Map<String, Object> out = new HashMap<>();
        out.put("passCode", pass.getPassCode());
        out.put("portalUrl", qrService.portalUrl(pass.getPassCode()));
        out.put("qrPngPath", "/api/v1/ipd/family/passes/" + pass.getId() + "/qr.png");
        return out;
    }

    public byte[] passQrPng(Long passId) {
        VisitorPass pass = passRepository.findByIdAndTenantIdAndShopId(
                        passId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Visitor pass not found"));
        return qrService.png(pass.getPassCode());
    }

    /** Public QR by pass code only. */
    public byte[] publicPassQrPng(String passCode) {
        VisitorPass pass = passRepository.findFirstByPassCodeIgnoreCase(passCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Pass not found"));
        return qrService.png(pass.getPassCode());
    }

    private Map<String, Object> buildDashboard(VisitorPass pass) {
        if (!"ACTIVE".equalsIgnoreCase(pass.getStatus())) {
            throw new IllegalStateException("Pass is " + pass.getStatus());
        }
        if (pass.getValidTo() != null && pass.getValidTo().isBefore(LocalDateTime.now())) {
            pass.setStatus("EXPIRED");
            passRepository.save(pass);
            throw new IllegalStateException("Pass expired");
        }
        IpdAdmission a = admissionRepository.findById(pass.getAdmissionId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
        String bedCode = null;
        if (a.getBedId() != null) {
            try {
                AccommodationBedDto bed = accommodationClient.getBed(a.getBedId());
                if (bed != null) {
                    bedCode = bed.getBedCode();
                }
            } catch (Exception ignored) {
                // Public view still works without bed detail
            }
        }
        List<IpdChargeLine> charges = chargeRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByChargeDateDesc(
                a.getTenantId(), a.getShopId(), a.getId());
        double pending = charges.stream()
                .filter(c -> !"VOID".equalsIgnoreCase(c.getStatus()))
                .mapToDouble(c -> c.getAmount() != null ? c.getAmount().doubleValue() : 0)
                .sum();

        Map<String, Object> out = new HashMap<>();
        out.put("visitorName", pass.getVisitorName());
        out.put("relation", pass.getRelation());
        out.put("visitingHours", pass.getVisitingHours());
        out.put("passCode", pass.getPassCode());
        out.put("portalUrl", qrService.portalUrl(pass.getPassCode()));
        out.put("patientName", a.getPatientName());
        out.put("admissionNo", a.getAdmissionNo());
        out.put("status", a.getStatus());
        out.put("bedCode", bedCode);
        out.put("diagnosis", a.getDiagnosis());
        out.put("estimatedDischarge", a.getExpectedStayDays());
        out.put("pendingBillAmount", pending);
        out.put("chargeCount", charges.size());
        return out;
    }

    private IpdAdmission assertAdmission(Long admissionId) {
        return admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }

    private static String generatePassCode() {
        int n = 100000 + RANDOM.nextInt(900000);
        return "VP" + n;
    }
}
