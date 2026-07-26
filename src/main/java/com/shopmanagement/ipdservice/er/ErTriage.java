package com.shopmanagement.ipdservice.er;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "er_triage")
public class ErTriage extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "patient_name", length = 191)
    private String patientName;

    /** ESI1..ESI5 or RED/YELLOW/GREEN style acuity codes */
    @Column(nullable = false, length = 16)
    private String acuity = "ESI3";

    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    /** WAITING | IN_TREATMENT | ADMITTED | DISCHARGED | LEFT */
    @Column(nullable = false, length = 32)
    private String status = "WAITING";

    @Column(name = "arrival_at", nullable = false)
    private LocalDateTime arrivalAt = LocalDateTime.now();

    @Column(name = "linked_admission_id")
    private Long linkedAdmissionId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getAcuity() { return acuity; }
    public void setAcuity(String acuity) { this.acuity = acuity; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getArrivalAt() { return arrivalAt; }
    public void setArrivalAt(LocalDateTime arrivalAt) { this.arrivalAt = arrivalAt; }
    public Long getLinkedAdmissionId() { return linkedAdmissionId; }
    public void setLinkedAdmissionId(Long linkedAdmissionId) { this.linkedAdmissionId = linkedAdmissionId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
