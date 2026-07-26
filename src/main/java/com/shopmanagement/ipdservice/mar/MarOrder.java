package com.shopmanagement.ipdservice.mar;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mar_order")
public class MarOrder extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "medicine_name", nullable = false, length = 255)
    private String medicineName;

    @Column(length = 64)
    private String dose;

    @Column(length = 32)
    private String route;

    @Column(length = 64)
    private String frequency;

    /** Comma-separated HH:mm slots e.g. 08:00,14:00,20:00 */
    @Column(name = "schedule_times", length = 255)
    private String scheduleTimes;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt = LocalDateTime.now();

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "ordered_by", length = 128)
    private String orderedBy;

    @Column(length = 128)
    private String barcode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Optional link to pharmacy product catalog for stock deduction on GIVEN. */
    @Column(name = "product_id")
    private Long productId;

    /** Base-unit quantity to deduct from stock on each GIVEN administration. */
    @Column(name = "dispense_quantity")
    private Integer dispenseQuantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDose() { return dose; }
    public void setDose(String dose) { this.dose = dose; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getScheduleTimes() { return scheduleTimes; }
    public void setScheduleTimes(String scheduleTimes) { this.scheduleTimes = scheduleTimes; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOrderedBy() { return orderedBy; }
    public void setOrderedBy(String orderedBy) { this.orderedBy = orderedBy; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getDispenseQuantity() { return dispenseQuantity; }
    public void setDispenseQuantity(Integer dispenseQuantity) { this.dispenseQuantity = dispenseQuantity; }
}
