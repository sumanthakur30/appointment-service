package com.shopmanagement.ipdservice.abha;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class IpdAbhaService {

    private static final Set<String> CONSENT = Set.of("PENDING", "GRANTED", "REVOKED");

    private final IpdAbhaLinkRepository linkRepository;
    private final IpdNdhmAbhaClient ndhmClient;
    private final boolean enabled;

    public IpdAbhaService(
            IpdAbhaLinkRepository linkRepository,
            IpdNdhmAbhaClient ndhmClient,
            @Value("${ipd.abha.enabled:true}") boolean enabled) {
        this.linkRepository = linkRepository;
        this.ndhmClient = ndhmClient;
        this.enabled = enabled;
    }

    public Map<String, Object> ndhmStatus() {
        requireEnabled();
        return ndhmClient.status();
    }

    public IpdAbhaLink findByPatient(Long patientId) {
        requireEnabled();
        requirePatient(patientId);
        return linkRepository
                .findByTenantIdAndShopIdAndPatientIdAndActiveTrue(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), patientId)
                .orElse(null);
    }

    @Transactional
    public IpdAbhaLink link(Long patientId, IpdAbhaLink body) {
        requireEnabled();
        requirePatient(patientId);
        if (body == null || body.getAbhaNumber() == null || body.getAbhaNumber().isBlank()) {
            throw new IllegalArgumentException("abhaNumber is required");
        }
        Map<String, Object> verified = ndhmClient.verifyAbhaNumber(body.getAbhaNumber());
        String consent = body.getConsentStatus() == null || body.getConsentStatus().isBlank()
                ? "PENDING"
                : body.getConsentStatus().trim().toUpperCase(Locale.ROOT);
        if (!CONSENT.contains(consent)) {
            throw new IllegalArgumentException("consentStatus must be PENDING|GRANTED|REVOKED");
        }

        IpdAbhaLink link = linkRepository
                .findByTenantIdAndShopIdAndPatientId(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), patientId)
                .orElseGet(IpdAbhaLink::new);
        if (link.getId() == null) {
            link.setTenantId(TenantContext.requireTenantId());
            link.setShopId(TenantContext.requireShopId());
            link.setPatientId(patientId);
        }
        link.setAbhaNumber(String.valueOf(verified.get("abhaNumber")));
        Object addr = verified.get("abhaAddress");
        link.setAbhaAddress(
                body.getAbhaAddress() != null && !body.getAbhaAddress().isBlank()
                        ? body.getAbhaAddress().trim()
                        : (addr != null ? String.valueOf(addr) : null));
        link.setConsentStatus(consent);
        if ("GRANTED".equals(consent) && link.getConsentAt() == null) {
            link.setConsentAt(LocalDateTime.now());
        }
        if ("REVOKED".equals(consent)) {
            link.setConsentAt(null);
        }
        link.setNdhmTxnId(verified.get("txnId") != null ? String.valueOf(verified.get("txnId")) : null);
        link.setNdhmMode(verified.get("mode") != null ? String.valueOf(verified.get("mode")) : null);
        link.setNotes(body.getNotes());
        link.setLinkedBy(TenantContext.currentActor());
        link.setActive(true);
        return linkRepository.save(link);
    }

    public Map<String, Object> requestOtp(Long patientId) {
        requireEnabled();
        IpdAbhaLink link = requireLink(patientId);
        return ndhmClient.requestConsentOtp(link.getAbhaNumber());
    }

    @Transactional
    public IpdAbhaLink confirmConsent(Long patientId, String txnId, String otp) {
        requireEnabled();
        IpdAbhaLink link = requireLink(patientId);
        Map<String, Object> confirmed = ndhmClient.confirmConsentOtp(txnId, otp);
        link.setConsentStatus("GRANTED");
        link.setConsentAt(LocalDateTime.now());
        if (confirmed.get("txnId") != null) {
            link.setNdhmTxnId(String.valueOf(confirmed.get("txnId")));
        }
        return linkRepository.save(link);
    }

    private IpdAbhaLink requireLink(Long patientId) {
        requirePatient(patientId);
        return linkRepository
                .findByTenantIdAndShopIdAndPatientIdAndActiveTrue(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), patientId)
                .orElseThrow(() -> new IllegalArgumentException("ABHA link not found for patient"));
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new IllegalStateException("IPD ABHA is disabled (ipd.abha.enabled=false)");
        }
    }

    private static void requirePatient(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("patientId is required");
        }
    }
}
