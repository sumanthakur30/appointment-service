package com.shopmanagement.ipdservice.cpoe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionChartService;
import com.shopmanagement.ipdservice.mar.MarOrder;
import com.shopmanagement.ipdservice.mar.MarService;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class CpoeOrderSetService {

    private final CpoeOrderSetRepository repository;
    private final MarService marService;
    private final IpdAdmissionChartService chartService;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public CpoeOrderSetService(
            CpoeOrderSetRepository repository,
            MarService marService,
            IpdAdmissionChartService chartService,
            ObjectMapper objectMapper,
            @Value("${ipd.cpoe.order-sets-enabled:true}") boolean enabled) {
        this.repository = repository;
        this.marService = marService;
        this.chartService = chartService;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    public List<CpoeOrderSet> list() {
        ensureDefaults();
        return repository.findByTenantIdAndShopIdAndActiveTrueOrderByNameAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    @Transactional
    public CpoeOrderSet upsert(CpoeOrderSet incoming) {
        if (!enabled) {
            throw new IllegalStateException("CPOE order sets disabled");
        }
        if (incoming.getCode() == null || incoming.getCode().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (incoming.getName() == null || incoming.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (incoming.getDefinitionJson() == null || incoming.getDefinitionJson().isBlank()) {
            throw new IllegalArgumentException("definitionJson is required");
        }
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        CpoeOrderSet row = repository
                .findByTenantIdAndShopIdAndCodeIgnoreCase(tenantId, shopId, incoming.getCode().trim())
                .orElseGet(CpoeOrderSet::new);
        row.setTenantId(tenantId);
        row.setShopId(shopId);
        row.setCode(incoming.getCode().trim().toUpperCase());
        row.setName(incoming.getName().trim());
        row.setSpecialty(incoming.getSpecialty());
        row.setDefinitionJson(incoming.getDefinitionJson());
        row.setActive(true);
        return repository.save(row);
    }

    @Transactional
    public Map<String, Object> apply(Long admissionId, String code) {
        if (!enabled) {
            throw new IllegalStateException("CPOE order sets disabled");
        }
        ensureDefaults();
        CpoeOrderSet set = repository
                .findByTenantIdAndShopIdAndCodeIgnoreCase(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), code)
                .orElseThrow(() -> new IllegalArgumentException("Order set not found: " + code));
        Map<String, Object> def = parse(set.getDefinitionJson());
        List<Object> marCreated = new ArrayList<>();
        List<Object> labsCreated = new ArrayList<>();

        Object marObj = def.get("mar");
        if (marObj instanceof List<?> marList) {
            for (Object item : marList) {
                if (!(item instanceof Map<?, ?> m)) {
                    continue;
                }
                MarOrder order = new MarOrder();
                order.setMedicineName(asString(m.get("medicineName")));
                order.setDose(asString(m.get("dose")));
                order.setRoute(asString(m.get("route")) != null ? asString(m.get("route")) : "ORAL");
                order.setFrequency(asString(m.get("frequency")) != null ? asString(m.get("frequency")) : "OD");
                order.setScheduleTimes(asString(m.get("scheduleTimes")));
                if (m.get("productId") instanceof Number n) {
                    order.setProductId(n.longValue());
                }
                if (m.get("dispenseQuantity") instanceof Number n) {
                    order.setDispenseQuantity(n.intValue());
                }
                if (order.getMedicineName() == null || order.getMedicineName().isBlank()) {
                    continue;
                }
                marCreated.add(marService.createOrder(admissionId, order));
            }
        }

        Object labsObj = def.get("labs");
        if (labsObj instanceof List<?> labList && !labList.isEmpty()) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (Object item : labList) {
                if (item instanceof Map<?, ?> m) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("testCode", m.get("testCode"));
                    row.put("testName", m.get("testName"));
                    items.add(row);
                }
            }
            if (!items.isEmpty()) {
                Map<String, Object> req = new LinkedHashMap<>();
                req.put("items", items);
                req.put("notes", "CPOE order set " + set.getCode());
                labsCreated.add(chartService.placeLabOrder(admissionId, req));
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("orderSet", set.getCode());
        out.put("marCreated", marCreated.size());
        out.put("labOrdersCreated", labsCreated.size());
        out.put("mar", marCreated);
        out.put("labs", labsCreated);
        return out;
    }

    private void ensureDefaults() {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        if (repository.findByTenantIdAndShopIdAndCodeIgnoreCase(tenantId, shopId, "ADMISSION_BASIC").isPresent()) {
            return;
        }
        CpoeOrderSet basic = new CpoeOrderSet();
        basic.setTenantId(tenantId);
        basic.setShopId(shopId);
        basic.setCode("ADMISSION_BASIC");
        basic.setName("Admission basics");
        basic.setSpecialty("GENERAL");
        basic.setDefinitionJson("""
                {"mar":[{"medicineName":"Paracetamol","dose":"500mg","route":"ORAL","frequency":"TID","scheduleTimes":"08:00,14:00,20:00"}],"labs":[{"testCode":"CBC","testName":"Complete Blood Count"},{"testCode":"RBS","testName":"Random Blood Sugar"}]}
                """);
        basic.setActive(true);
        repository.save(basic);

        if (repository.findByTenantIdAndShopIdAndCodeIgnoreCase(tenantId, shopId, "FEVER_PANEL").isEmpty()) {
            CpoeOrderSet fever = new CpoeOrderSet();
            fever.setTenantId(tenantId);
            fever.setShopId(shopId);
            fever.setCode("FEVER_PANEL");
            fever.setName("Fever workup");
            fever.setSpecialty("MEDICINE");
            fever.setDefinitionJson("""
                    {"mar":[{"medicineName":"Paracetamol","dose":"650mg","route":"ORAL","frequency":"QID"}],"labs":[{"testCode":"CBC","testName":"Complete Blood Count"},{"testCode":"CRP","testName":"C-Reactive Protein"},{"testCode":"MP","testName":"Malaria Parasite"}]}
                    """);
            fever.setActive(true);
            repository.save(fever);
        }
    }

    private Map<String, Object> parse(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid order set definition JSON");
        }
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
