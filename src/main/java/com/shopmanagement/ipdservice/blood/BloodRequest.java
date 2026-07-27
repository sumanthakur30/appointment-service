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
@Table(name = "blood_request")
public class BloodRequest extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id")
    private Long admissionId;

    @Column(name = "ot_booking_id")
    private Long otBookingId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "blood_group", nullable = false, length = 8)
    private String bloodGroup;

    @Column(nullable = false, length = 32)
    private String component = "PRBC";

    @Column(name = "units_requested", nullable = false)
    private int unitsRequested = 1;

    @Column(name = "clinical_indication", columnDefinition = "TEXT")
    private String clinicalIndication;

    @Column(nullable = false, length = 32)
    private String status = "REQUESTED";

    @Column(name = "matched_unit_id")
    private Long matchedUnitId;

    @Column(name = "requested_by", length = 128)
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "crossmatched_at")
    private LocalDateTime crossmatchedAt;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public Long getOtBookingId() { return otBookingId; }
    public void setOtBookingId(Long otBookingId) { this.otBookingId = otBookingId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public int getUnitsRequested() { return unitsRequested; }
    public void setUnitsRequested(int unitsRequested) { this.unitsRequested = unitsRequested; }
    public String getClinicalIndication() { return clinicalIndication; }
    public void setClinicalIndication(String clinicalIndication) { this.clinicalIndication = clinicalIndication; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getMatchedUnitId() { return matchedUnitId; }
    public void setMatchedUnitId(Long matchedUnitId) { this.matchedUnitId = matchedUnitId; }
    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public LocalDateTime getCrossmatchedAt() { return crossmatchedAt; }
    public void setCrossmatchedAt(LocalDateTime crossmatchedAt) { this.crossmatchedAt = crossmatchedAt; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
