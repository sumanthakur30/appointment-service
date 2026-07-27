package com.shopmanagement.ipdservice.ot;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ot_implant_usage")
public class OtImplantUsage extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ot_booking_id", nullable = false)
    private Long otBookingId;

    @Column(name = "implant_sku", nullable = false, length = 128)
    private String implantSku;

    @Column(name = "implant_name", length = 255)
    private String implantName;

    @Column(name = "lot_number", length = 128)
    private String lotNumber;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(length = 32)
    private String laterality;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "recorded_by", length = 128)
    private String recordedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOtBookingId() { return otBookingId; }
    public void setOtBookingId(Long otBookingId) { this.otBookingId = otBookingId; }
    public String getImplantSku() { return implantSku; }
    public void setImplantSku(String implantSku) { this.implantSku = implantSku; }
    public String getImplantName() { return implantName; }
    public void setImplantName(String implantName) { this.implantName = implantName; }
    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getLaterality() { return laterality; }
    public void setLaterality(String laterality) { this.laterality = laterality; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}
