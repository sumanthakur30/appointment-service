package com.shopmanagement.ipdservice.catalogue;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ipd_code_catalogue")
public class CodeCatalogueEntry extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "system_code", nullable = false, length = 32)
    private String systemCode;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 512)
    private String display;

    @Column(name = "search_text", length = 1024)
    private String searchText;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSystemCode() { return systemCode; }
    public void setSystemCode(String systemCode) { this.systemCode = systemCode; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }
    public String getSearchText() { return searchText; }
    public void setSearchText(String searchText) { this.searchText = searchText; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
