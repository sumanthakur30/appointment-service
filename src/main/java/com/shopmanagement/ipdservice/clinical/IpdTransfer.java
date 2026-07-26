package com.shopmanagement.ipdservice.clinical;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ipd_transfer")
public class IpdTransfer extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "from_bed_id", nullable = false)
    private Long fromBedId;

    @Column(name = "to_bed_id", nullable = false)
    private Long toBedId;

    @Column(length = 512)
    private String reason;

    @Column(nullable = false, length = 32)
    private String status = "COMPLETED";

    @Column(name = "transferred_at", nullable = false)
    private LocalDateTime transferredAt = LocalDateTime.now();

    @Column(name = "approved_by", length = 128)
    private String approvedBy;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public Long getFromBedId() { return fromBedId; }
    public void setFromBedId(Long fromBedId) { this.fromBedId = fromBedId; }
    public Long getToBedId() { return toBedId; }
    public void setToBedId(Long toBedId) { this.toBedId = toBedId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTransferredAt() { return transferredAt; }
    public void setTransferredAt(LocalDateTime transferredAt) { this.transferredAt = transferredAt; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
