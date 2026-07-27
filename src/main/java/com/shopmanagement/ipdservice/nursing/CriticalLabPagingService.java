package com.shopmanagement.ipdservice.nursing;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.clinical.IpdLabClient;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class CriticalLabPagingService {

    private final CriticalLabAlertRepository alertRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final IpdLabClient labClient;
    private final boolean enabled;

    public CriticalLabPagingService(
            CriticalLabAlertRepository alertRepository,
            IpdAdmissionRepository admissionRepository,
            IpdLabClient labClient,
            @Value("${ipd.lab.critical-paging-enabled:true}") boolean enabled) {
        this.alertRepository = alertRepository;
        this.admissionRepository = admissionRepository;
        this.labClient = labClient;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<CriticalLabAlert> openAlerts() {
        return alertRepository.findByTenantIdAndShopIdAndStatusOrderByCreatedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), "OPEN");
    }

    public List<CriticalLabAlert> forAdmission(Long admissionId) {
        return alertRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByCreatedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    @Transactional
    public CriticalLabAlert acknowledge(Long id) {
        CriticalLabAlert a = alertRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Critical lab alert not found"));
        a.setStatus("ACKED");
        a.setAckedAt(LocalDateTime.now());
        a.setAckedBy(TenantContext.currentActor());
        return alertRepository.save(a);
    }

    /**
     * Scan active ward admissions' lab summaries and upsert CRITICAL alerts.
     */
    @Transactional
    public Map<String, Object> refreshFromLabs() {
        if (!enabled) {
            return Map.of("enabled", false, "created", 0, "open", openAlerts().size());
        }
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        List<IpdAdmission> ward = admissionRepository.findByTenantIdAndShopIdAndStatusInOrderByAdmittedAtDesc(
                tenantId, shopId, List.of("ADMITTED", "TRANSFERRED"));
        int created = 0;
        List<Map<String, Object>> samples = new ArrayList<>();
        for (IpdAdmission admission : ward) {
            Map<String, Object> summary = labClient.patientClinicalSummary(admission.getPatientId(), tenantId);
            Object resultsObj = summary.get("labResults");
            if (!(resultsObj instanceof List<?> results)) {
                continue;
            }
            for (Object rowObj : results) {
                if (!(rowObj instanceof Map<?, ?> row)) {
                    continue;
                }
                if (!isCritical(row)) {
                    continue;
                }
                String resultKey = resultKey(row);
                if (alertRepository.findByTenantIdAndShopIdAndLabResultId(tenantId, shopId, resultKey).isPresent()) {
                    continue;
                }
                CriticalLabAlert alert = new CriticalLabAlert();
                alert.setTenantId(tenantId);
                alert.setShopId(shopId);
                alert.setAdmissionId(admission.getId());
                alert.setPatientId(admission.getPatientId());
                alert.setLabOrderId(asLong(row.get("labOrderId")));
                alert.setLabResultId(resultKey);
                alert.setParameterName(firstNonBlank(row, "parameterName", "testName", "analyte", "name"));
                alert.setResultValue(firstNonBlank(row, "value", "resultValue", "result"));
                alert.setFlag("CRITICAL");
                alert.setStatus("OPEN");
                alert.setDetail(String.valueOf(row));
                alertRepository.save(alert);
                created++;
                Map<String, Object> sample = new LinkedHashMap<>();
                sample.put("admissionId", admission.getId());
                sample.put("parameter", alert.getParameterName());
                samples.add(sample);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", true);
        out.put("created", created);
        out.put("open", openAlerts().size());
        out.put("samples", samples);
        return out;
    }

    private static boolean isCritical(Map<?, ?> row) {
        for (String key : List.of("flag", "autoFlag", "resultFlag", "severity", "critical")) {
            Object v = row.get(key);
            if (v == null) {
                continue;
            }
            if (v instanceof Boolean b && b) {
                return true;
            }
            String s = String.valueOf(v).trim().toUpperCase(Locale.ROOT);
            if (s.contains("CRITICAL") || s.equals("C") || s.equals("TRUE") || s.equals("HIGH_CRITICAL")
                    || s.equals("LOW_CRITICAL")) {
                return true;
            }
        }
        return false;
    }

    private static String resultKey(Map<?, ?> row) {
        Object id = row.get("id");
        if (id != null) {
            return String.valueOf(id);
        }
        return String.valueOf(row.get("labOrderId")) + ":"
                + firstNonBlank(row, "parameterName", "testName", "analyte", "name") + ":"
                + firstNonBlank(row, "value", "resultValue", "result");
    }

    private static String firstNonBlank(Map<?, ?> row, String... keys) {
        for (String k : keys) {
            Object v = row.get(k);
            if (v != null && !String.valueOf(v).isBlank()) {
                return String.valueOf(v);
            }
        }
        return "";
    }

    private static Long asLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception ex) {
            return null;
        }
    }
}
