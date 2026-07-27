package com.shopmanagement.ipdservice.blood;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "blood_unit")
public class BloodUnit extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_code", nullable = false, length = 64)
    private String unitCode;

    @Column(name = "blood_group", nullable = false, length = 8)
    private String bloodGroup;

    @Column(nullable = false, length = 32)
    private String component = "PRBC";

    @Column(nullable = false, length = 32)
    private String status = "AVAILABLE";

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "donor_ref", length = 128)
    private String donorRef;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getDonorRef() { return donorRef; }
    public void setDonorRef(String donorRef) { this.donorRef = donorRef; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
