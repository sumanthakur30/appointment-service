package com.shopmanagement.ipdservice.catalogue;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class CodeCatalogueService {

    private final CodeCatalogueRepository repository;
    private final boolean icdEnabled;

    public CodeCatalogueService(
            CodeCatalogueRepository repository,
            @Value("${ipd.icd.catalogue-enabled:true}") boolean icdEnabled) {
        this.repository = repository;
        this.icdEnabled = icdEnabled;
    }

    public List<CodeCatalogueEntry> searchIcd(String q, int limit) {
        if (!icdEnabled) {
            return List.of();
        }
        ensureIcdSeed();
        String query = q == null ? "" : q.trim();
        if (query.length() < 1) {
            return List.of();
        }
        List<CodeCatalogueEntry> rows = repository.search(
                TenantContext.requireTenantId(),
                TenantContext.requireShopId(),
                "ICD10",
                query);
        int cap = Math.min(Math.max(limit, 1), 50);
        return rows.size() > cap ? rows.subList(0, cap) : rows;
    }

    @Transactional
    public CodeCatalogueEntry upsert(CodeCatalogueEntry incoming) {
        if (incoming.getSystemCode() == null || incoming.getSystemCode().isBlank()) {
            throw new IllegalArgumentException("systemCode is required");
        }
        if (incoming.getCode() == null || incoming.getCode().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (incoming.getDisplay() == null || incoming.getDisplay().isBlank()) {
            throw new IllegalArgumentException("display is required");
        }
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        String system = incoming.getSystemCode().trim().toUpperCase(Locale.ROOT);
        String code = incoming.getCode().trim().toUpperCase(Locale.ROOT);
        CodeCatalogueEntry row = repository
                .findByTenantIdAndShopIdAndSystemCodeAndCodeIgnoreCase(tenantId, shopId, system, code)
                .orElseGet(CodeCatalogueEntry::new);
        row.setTenantId(tenantId);
        row.setShopId(shopId);
        row.setSystemCode(system);
        row.setCode(code);
        row.setDisplay(incoming.getDisplay().trim());
        row.setSearchText((code + " " + row.getDisplay()).toLowerCase(Locale.ROOT));
        row.setActive(true);
        return repository.save(row);
    }

    @Transactional
    public void ensureIcdSeed() {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        if (repository.findByTenantIdAndShopIdAndSystemCodeAndCodeIgnoreCase(tenantId, shopId, "ICD10", "J18.9")
                .isPresent()) {
            return;
        }
        seed(tenantId, shopId, "J18.9", "Pneumonia, unspecified organism");
        seed(tenantId, shopId, "I10", "Essential (primary) hypertension");
        seed(tenantId, shopId, "E11.9", "Type 2 diabetes mellitus without complications");
        seed(tenantId, shopId, "N39.0", "Urinary tract infection, site not specified");
        seed(tenantId, shopId, "A09", "Infectious gastroenteritis and colitis, unspecified");
        seed(tenantId, shopId, "K35.80", "Unspecified acute appendicitis");
        seed(tenantId, shopId, "O80", "Encounter for full-term uncomplicated delivery");
        seed(tenantId, shopId, "S06.0X0A", "Concussion without loss of consciousness, initial encounter");
        seed(tenantId, shopId, "J45.909", "Unspecified asthma, uncomplicated");
        seed(tenantId, shopId, "Z51.11", "Encounter for antineoplastic chemotherapy");
    }

    private void seed(Long tenantId, String shopId, String code, String display) {
        CodeCatalogueEntry e = new CodeCatalogueEntry();
        e.setTenantId(tenantId);
        e.setShopId(shopId);
        e.setSystemCode("ICD10");
        e.setCode(code);
        e.setDisplay(display);
        e.setSearchText((code + " " + display).toLowerCase(Locale.ROOT));
        e.setActive(true);
        repository.save(e);
    }
}
