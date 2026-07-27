package com.shopmanagement.ipdservice.pharmacy;

import java.math.BigDecimal;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ward_imprest_location")
public class WardImprestLocation extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ward_code", nullable = false, length = 64)
    private String wardCode;

    @Column(name = "ward_name", length = 191)
    private String wardName;

    @Column(name = "warehouse_ref", length = 128)
    private String warehouseRef;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public String getWarehouseRef() { return warehouseRef; }
    public void setWarehouseRef(String warehouseRef) { this.warehouseRef = warehouseRef; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
