package com.shopmanagement.ipdservice.pharmacy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "controlled_drug_register_entry")
public class ControlledDrugRegisterEntry extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id")
    private Long admissionId;

    @Column(name = "ward_code", length = 64)
    private String wardCode;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "medicine_name", nullable = false, length = 255)
    private String medicineName;

    @Column(name = "batch_no", length = 64)
    private String batchNo;

    /** ISSUE | ADMIN | WASTE | RETURN | COUNT */
    @Column(name = "txn_type", nullable = false, length = 32)
    private String txnType;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(length = 32)
    private String unit = "UNIT";

    @Column(name = "nurse_id", length = 128)
    private String nurseId;

    @Column(name = "witness_id", length = 128)
    private String witnessId;

    @Column(name = "mar_administration_id")
    private Long marAdministrationId;

    @Column(name = "balance_after", precision = 12, scale = 3)
    private BigDecimal balanceAfter;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }
    public String getWitnessId() { return witnessId; }
    public void setWitnessId(String witnessId) { this.witnessId = witnessId; }
    public Long getMarAdministrationId() { return marAdministrationId; }
    public void setMarAdministrationId(Long marAdministrationId) { this.marAdministrationId = marAdministrationId; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
