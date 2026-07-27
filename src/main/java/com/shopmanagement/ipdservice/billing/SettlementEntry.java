package com.shopmanagement.ipdservice.billing;

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
@Table(name = "ipd_settlement_entry")
public class SettlementEntry extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "bill_id")
    private Long billId;

    /** DEPOSIT | PATIENT_PAY | TPA_APPROVED | TPA_RECEIVED | ADJUSTMENT | REFUND */
    @Column(name = "entry_type", nullable = false, length = 32)
    private String entryType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    /** CREDIT reduces amount due; DEBIT increases */
    @Column(nullable = false, length = 8)
    private String direction = "CREDIT";

    @Column(name = "reference_no", length = 128)
    private String referenceNo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 32)
    private String status = "POSTED";

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt = LocalDateTime.now();

    @Column(name = "posted_by", length = 128)
    private String postedBy;

    /** PENDING | SYNCED | FAILED | SKIPPED */
    @Column(name = "sync_status", nullable = false, length = 16)
    private String syncStatus = "PENDING";

    @Column(name = "external_payment_id")
    private Long externalPaymentId;

    @Column(name = "external_order_ids", columnDefinition = "TEXT")
    private String externalOrderIds;

    @Column(name = "sync_error", columnDefinition = "TEXT")
    private String syncError;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public Long getExternalPaymentId() { return externalPaymentId; }
    public void setExternalPaymentId(Long externalPaymentId) { this.externalPaymentId = externalPaymentId; }
    public String getExternalOrderIds() { return externalOrderIds; }
    public void setExternalOrderIds(String externalOrderIds) { this.externalOrderIds = externalOrderIds; }
    public String getSyncError() { return syncError; }
    public void setSyncError(String syncError) { this.syncError = syncError; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
}
