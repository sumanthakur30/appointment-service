package com.shopmanagement.ipdservice.quality;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quality_incident")
public class QualityIncident extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id")
    private Long admissionId;

    @Column(name = "incident_type", nullable = false, length = 64)
    private String incidentType;

    @Column(nullable = false, length = 32)
    private String severity = "MEDIUM";

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 32)
    private String status = "OPEN";

    @Column(name = "nabh_indicator_code", length = 64)
    private String nabhIndicatorCode;

    @Column(name = "capa_notes", columnDefinition = "TEXT")
    private String capaNotes;

    @Column(name = "reported_by", length = 128)
    private String reportedBy;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getIncidentType() { return incidentType; }
    public void setIncidentType(String incidentType) { this.incidentType = incidentType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNabhIndicatorCode() { return nabhIndicatorCode; }
    public void setNabhIndicatorCode(String nabhIndicatorCode) { this.nabhIndicatorCode = nabhIndicatorCode; }
    public String getCapaNotes() { return capaNotes; }
    public void setCapaNotes(String capaNotes) { this.capaNotes = capaNotes; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
