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
@Table(name = "mar_administration")
public class MarAdministration extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mar_order_id", nullable = false)
    private Long marOrderId;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "administered_at")
    private LocalDateTime administeredAt;

    @Column(name = "dose_given", length = 64)
    private String doseGiven;

    /** GIVEN | MISSED | DELAYED | REFUSED | HELD */
    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "nurse_id", length = 128)
    private String nurseId;

    @Column(length = 512)
    private String reason;

    @Column(name = "barcode_scanned", length = 128)
    private String barcodeScanned;

    @Column(name = "stock_reservation_key", length = 120)
    private String stockReservationKey;

    /** SKIPPED | COMMITTED | FAILED | NONE */
    @Column(name = "stock_status", length = 32)
    private String stockStatus;

    @Column(name = "stock_detail", columnDefinition = "TEXT")
    private String stockDetail;

    @Column(name = "allergy_override", nullable = false)
    private boolean allergyOverride;

    @Column(name = "allergy_match", columnDefinition = "TEXT")
    private String allergyMatch;

    @Column(name = "five_rights_verified", nullable = false)
    private boolean fiveRightsVerified;

    @Column(name = "five_rights_detail", columnDefinition = "TEXT")
    private String fiveRightsDetail;

    @Column(name = "patient_id_confirmed", length = 64)
    private String patientIdConfirmed;

    @Column(name = "witness_id", length = 128)
    private String witnessId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMarOrderId() { return marOrderId; }
    public void setMarOrderId(Long marOrderId) { this.marOrderId = marOrderId; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public LocalDateTime getAdministeredAt() { return administeredAt; }
    public void setAdministeredAt(LocalDateTime administeredAt) { this.administeredAt = administeredAt; }
    public String getDoseGiven() { return doseGiven; }
    public void setDoseGiven(String doseGiven) { this.doseGiven = doseGiven; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getBarcodeScanned() { return barcodeScanned; }
    public void setBarcodeScanned(String barcodeScanned) { this.barcodeScanned = barcodeScanned; }
    public String getStockReservationKey() { return stockReservationKey; }
    public void setStockReservationKey(String stockReservationKey) { this.stockReservationKey = stockReservationKey; }
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    public String getStockDetail() { return stockDetail; }
    public void setStockDetail(String stockDetail) { this.stockDetail = stockDetail; }
    public boolean isAllergyOverride() { return allergyOverride; }
    public void setAllergyOverride(boolean allergyOverride) { this.allergyOverride = allergyOverride; }
    public String getAllergyMatch() { return allergyMatch; }
    public void setAllergyMatch(String allergyMatch) { this.allergyMatch = allergyMatch; }
    public boolean isFiveRightsVerified() { return fiveRightsVerified; }
    public void setFiveRightsVerified(boolean fiveRightsVerified) { this.fiveRightsVerified = fiveRightsVerified; }
    public String getFiveRightsDetail() { return fiveRightsDetail; }
    public void setFiveRightsDetail(String fiveRightsDetail) { this.fiveRightsDetail = fiveRightsDetail; }
    public String getPatientIdConfirmed() { return patientIdConfirmed; }
    public void setPatientIdConfirmed(String patientIdConfirmed) { this.patientIdConfirmed = patientIdConfirmed; }
    public String getWitnessId() { return witnessId; }
    public void setWitnessId(String witnessId) { this.witnessId = witnessId; }
}
