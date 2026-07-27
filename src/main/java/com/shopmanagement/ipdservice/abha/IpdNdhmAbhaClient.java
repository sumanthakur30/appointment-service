package com.shopmanagement.ipdservice.abha;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ABDM/ABHA sandbox client for IPD. Live credentials can be added later without API changes.
 */
@Component
public class IpdNdhmAbhaClient {

    private final boolean enabled;
    private final String mode;
    private final ConcurrentHashMap<String, String> sandboxOtps = new ConcurrentHashMap<>();

    public IpdNdhmAbhaClient(
            @Value("${ipd.abha.enabled:true}") boolean enabled,
            @Value("${ipd.abha.mode:sandbox}") String mode) {
        this.enabled = enabled;
        this.mode = mode == null || mode.isBlank() ? "sandbox" : mode.trim().toLowerCase();
    }

    public Map<String, Object> status() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", enabled);
        out.put("mode", mode);
        out.put("message", enabled
                ? ("sandbox".equals(mode)
                        ? "NDHM sandbox — OTP 123456 for consent demos"
                        : "Live mode configured; wire credentials for production")
                : "IPD ABHA disabled");
        return out;
    }

    public Map<String, Object> verifyAbhaNumber(String abhaNumber) {
        requireEnabled();
        String number = normalize(abhaNumber);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", mode);
        out.put("status", "VALID");
        out.put("abhaNumber", number);
        out.put("abhaAddress", number.substring(0, 4) + "xxxx@sbx");
        out.put("txnId", mode + "-" + UUID.randomUUID());
        return out;
    }

    public Map<String, Object> requestConsentOtp(String abhaNumber) {
        requireEnabled();
        String number = normalize(abhaNumber);
        String txnId = "otp-" + UUID.randomUUID();
        sandboxOtps.put(txnId, "123456");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", mode);
        out.put("txnId", txnId);
        out.put("abhaNumber", number);
        out.put("message", "OTP sent (sandbox). Use 123456 to confirm.");
        return out;
    }

    public Map<String, Object> confirmConsentOtp(String txnId, String otp) {
        requireEnabled();
        if (txnId == null || txnId.isBlank()) {
            throw new IllegalArgumentException("txnId is required");
        }
        String expected = sandboxOtps.getOrDefault(txnId, "123456");
        if (otp == null || !expected.equals(otp.trim())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        sandboxOtps.remove(txnId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", mode);
        out.put("status", "GRANTED");
        out.put("txnId", txnId);
        return out;
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new IllegalStateException("IPD ABHA is disabled (ipd.abha.enabled=false)");
        }
    }

    private static String normalize(String abhaNumber) {
        String number = abhaNumber == null ? "" : abhaNumber.replaceAll("\\s|-", "");
        if (!number.matches("\\d{14}")) {
            throw new IllegalArgumentException("abhaNumber must be 14 digits");
        }
        return number;
    }
}
