package com.shopmanagement.ipdservice.abha;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ipd_patient_abha")
public class IpdAbhaLink extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "abha_number", nullable = false, length = 14)
    private String abhaNumber;

    @Column(name = "abha_address", length = 191)
    private String abhaAddress;

    @Column(name = "consent_status", nullable = false, length = 32)
    private String consentStatus = "PENDING";

    @Column(name = "consent_at")
    private LocalDateTime consentAt;

    @Column(name = "ndhm_txn_id", length = 128)
    private String ndhmTxnId;

    @Column(name = "ndhm_mode", length = 32)
    private String ndhmMode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "linked_by", length = 128)
    private String linkedBy;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getAbhaNumber() { return abhaNumber; }
    public void setAbhaNumber(String abhaNumber) { this.abhaNumber = abhaNumber; }
    public String getAbhaAddress() { return abhaAddress; }
    public void setAbhaAddress(String abhaAddress) { this.abhaAddress = abhaAddress; }
    public String getConsentStatus() { return consentStatus; }
    public void setConsentStatus(String consentStatus) { this.consentStatus = consentStatus; }
    public LocalDateTime getConsentAt() { return consentAt; }
    public void setConsentAt(LocalDateTime consentAt) { this.consentAt = consentAt; }
    public String getNdhmTxnId() { return ndhmTxnId; }
    public void setNdhmTxnId(String ndhmTxnId) { this.ndhmTxnId = ndhmTxnId; }
    public String getNdhmMode() { return ndhmMode; }
    public void setNdhmMode(String ndhmMode) { this.ndhmMode = ndhmMode; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getLinkedBy() { return linkedBy; }
    public void setLinkedBy(String linkedBy) { this.linkedBy = linkedBy; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
