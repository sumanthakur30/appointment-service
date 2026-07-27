package com.shopmanagement.ipdservice.cssd;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cssd_set")
public class CssdSet extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "set_code", nullable = false, length = 64)
    private String setCode;

    @Column(name = "set_name", nullable = false, length = 191)
    private String setName;

    @Column(length = 64)
    private String specialty;

    @Column(nullable = false, length = 32)
    private String status = "AVAILABLE";

    @Column(name = "last_sterilized_at")
    private LocalDateTime lastSterilizedAt;

    @Column(name = "issued_ot_booking_id")
    private Long issuedOtBookingId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSetCode() { return setCode; }
    public void setSetCode(String setCode) { this.setCode = setCode; }
    public String getSetName() { return setName; }
    public void setSetName(String setName) { this.setName = setName; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastSterilizedAt() { return lastSterilizedAt; }
    public void setLastSterilizedAt(LocalDateTime lastSterilizedAt) { this.lastSterilizedAt = lastSterilizedAt; }
    public Long getIssuedOtBookingId() { return issuedOtBookingId; }
    public void setIssuedOtBookingId(Long issuedOtBookingId) { this.issuedOtBookingId = issuedOtBookingId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
