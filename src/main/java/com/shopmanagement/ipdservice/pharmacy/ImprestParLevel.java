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
@Table(name = "imprest_par_level")
public class ImprestParLevel extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "imprest_location_id", nullable = false)
    private Long imprestLocationId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "medicine_name", nullable = false, length = 255)
    private String medicineName;

    @Column(name = "min_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal minQty = BigDecimal.ZERO;

    @Column(name = "par_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal parQty = BigDecimal.ZERO;

    @Column(name = "max_qty", precision = 12, scale = 3)
    private BigDecimal maxQty;

    @Column(name = "on_hand_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal onHandQty = BigDecimal.ZERO;

    @Column(length = 32)
    private String unit = "UNIT";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getImprestLocationId() { return imprestLocationId; }
    public void setImprestLocationId(Long imprestLocationId) { this.imprestLocationId = imprestLocationId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public BigDecimal getMinQty() { return minQty; }
    public void setMinQty(BigDecimal minQty) { this.minQty = minQty; }
    public BigDecimal getParQty() { return parQty; }
    public void setParQty(BigDecimal parQty) { this.parQty = parQty; }
    public BigDecimal getMaxQty() { return maxQty; }
    public void setMaxQty(BigDecimal maxQty) { this.maxQty = maxQty; }
    public BigDecimal getOnHandQty() { return onHandQty; }
    public void setOnHandQty(BigDecimal onHandQty) { this.onHandQty = onHandQty; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
