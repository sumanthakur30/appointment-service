package com.shopmanagement.ipdservice.quality;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class QualityIncidentService {

    private final QualityIncidentRepository incidentRepository;
    private final QualityIndicatorDefRepository indicatorDefRepository;
    private final boolean indicatorsEnabled;

    public QualityIncidentService(
            QualityIncidentRepository incidentRepository,
            QualityIndicatorDefRepository indicatorDefRepository,
            @Value("${ipd.quality.indicators-enabled:true}") boolean indicatorsEnabled) {
        this.incidentRepository = incidentRepository;
        this.indicatorDefRepository = indicatorDefRepository;
        this.indicatorsEnabled = indicatorsEnabled;
    }

    public List<QualityIncident> list() {
        return incidentRepository.findByTenantIdAndShopIdOrderByReportedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    @Transactional
    public QualityIncident create(QualityIncident incoming) {
        if (incoming.getTitle() == null || incoming.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (incoming.getIncidentType() == null || incoming.getIncidentType().isBlank()) {
            throw new IllegalArgumentException("incidentType is required");
        }
        QualityIncident row = new QualityIncident();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(incoming.getAdmissionId());
        row.setIncidentType(incoming.getIncidentType().trim().toUpperCase(Locale.ROOT));
        row.setSeverity(incoming.getSeverity() == null || incoming.getSeverity().isBlank()
                ? "MEDIUM" : incoming.getSeverity().trim().toUpperCase(Locale.ROOT));
        row.setTitle(incoming.getTitle().trim());
        row.setDescription(incoming.getDescription());
        row.setStatus("OPEN");
        row.setNabhIndicatorCode(incoming.getNabhIndicatorCode());
        row.setReportedBy(TenantContext.currentActor());
        row.setReportedAt(LocalDateTime.now());
        return incidentRepository.save(row);
    }

    @Transactional
    public QualityIncident close(Long id, String capaNotes) {
        QualityIncident row = incidentRepository
                .findByIdAndTenantIdAndShopId(id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        row.setStatus("CLOSED");
        row.setClosedAt(LocalDateTime.now());
        if (capaNotes != null && !capaNotes.isBlank()) {
            row.setCapaNotes(capaNotes.trim());
        }
        return incidentRepository.save(row);
    }

    @Transactional
    public Map<String, Object> indicatorSnapshot() {
        List<QualityIncident> all = list();
        long open = all.stream().filter(i -> "OPEN".equalsIgnoreCase(i.getStatus())).count();
        long closed = all.stream().filter(i -> "CLOSED".equalsIgnoreCase(i.getStatus())).count();
        long high = all.stream().filter(i -> "HIGH".equalsIgnoreCase(i.getSeverity())
                || "CRITICAL".equalsIgnoreCase(i.getSeverity())).count();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalIncidents", all.size());
        out.put("openIncidents", open);
        out.put("closedIncidents", closed);
        out.put("highOrCritical", high);
        out.put("indicators", buildIndicators(all, open, closed, high));
        return out;
    }

    private List<Map<String, Object>> buildIndicators(
            List<QualityIncident> all, long open, long closed, long high) {
        if (!indicatorsEnabled) {
            return List.of();
        }
        ensureDefaultIndicators();
        List<QualityIndicatorDef> defs = indicatorDefRepository
                .findByTenantIdAndShopIdAndActiveTrueOrderBySortOrderAsc(
                        TenantContext.requireTenantId(), TenantContext.requireShopId());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (QualityIndicatorDef def : defs) {
            long value = switch (def.getFormulaKey().toUpperCase(Locale.ROOT)) {
                case "COUNT_OPEN" -> open;
                case "COUNT_CLOSED" -> closed;
                case "COUNT_HIGH" -> high;
                default -> all.size();
            };
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", def.getCode());
            row.put("name", def.getName());
            row.put("formulaKey", def.getFormulaKey());
            row.put("value", value);
            rows.add(row);
        }
        return rows;
    }

    public void ensureDefaultIndicators() {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        if (indicatorDefRepository.findByTenantIdAndShopIdAndCodeIgnoreCase(tenantId, shopId, "NABH-QI-01")
                .isPresent()) {
            return;
        }
        saveDef(tenantId, shopId, "NABH-QI-01", "Patient safety incidents", "COUNT_TOTAL", 1);
        saveDef(tenantId, shopId, "NABH-QI-02", "Open CAPA items", "COUNT_OPEN", 2);
        saveDef(tenantId, shopId, "NABH-QI-03", "High/critical severity", "COUNT_HIGH", 3);
    }

    private void saveDef(Long tenantId, String shopId, String code, String name, String formula, int sort) {
        QualityIndicatorDef d = new QualityIndicatorDef();
        d.setTenantId(tenantId);
        d.setShopId(shopId);
        d.setCode(code);
        d.setName(name);
        d.setFormulaKey(formula);
        d.setSortOrder(sort);
        d.setActive(true);
        indicatorDefRepository.save(d);
    }
}
