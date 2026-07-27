package com.shopmanagement.ipdservice.infection;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hai_event")
public class HaiEvent extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    /** CLABSI | CAUTI | SSI | VAP | CDI | OTHER */
    @Column(name = "hai_type", nullable = false, length = 64)
    private String haiType;

    @Column(name = "onset_date")
    private LocalDate onsetDate;

    @Column(name = "device_type", length = 128)
    private String deviceType;

    @Column(name = "isolation_id")
    private Long isolationId;

    @Column(name = "quality_incident_id")
    private Long qualityIncidentId;

    @Column(nullable = false, length = 32)
    private String status = "OPEN";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by", length = 128)
    private String closedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getHaiType() { return haiType; }
    public void setHaiType(String haiType) { this.haiType = haiType; }
    public LocalDate getOnsetDate() { return onsetDate; }
    public void setOnsetDate(LocalDate onsetDate) { this.onsetDate = onsetDate; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public Long getIsolationId() { return isolationId; }
    public void setIsolationId(Long isolationId) { this.isolationId = isolationId; }
    public Long getQualityIncidentId() { return qualityIncidentId; }
    public void setQualityIncidentId(Long qualityIncidentId) { this.qualityIncidentId = qualityIncidentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }
}
