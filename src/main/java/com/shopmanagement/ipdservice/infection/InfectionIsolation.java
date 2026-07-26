package com.shopmanagement.ipdservice.infection;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "infection_isolation")
public class InfectionIsolation extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    /** CONTACT | DROPLET | AIRBORNE | MRSA | COVID | CUSTOM */
    @Column(name = "isolation_type", nullable = false, length = 64)
    private String isolationType;

    @Column(length = 128)
    private String pathogen;

    @Column(name = "ppe_required", length = 255)
    private String ppeRequired;

    @Column(name = "cleaning_notes", columnDefinition = "TEXT")
    private String cleaningNotes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getIsolationType() { return isolationType; }
    public void setIsolationType(String isolationType) { this.isolationType = isolationType; }
    public String getPathogen() { return pathogen; }
    public void setPathogen(String pathogen) { this.pathogen = pathogen; }
    public String getPpeRequired() { return ppeRequired; }
    public void setPpeRequired(String ppeRequired) { this.ppeRequired = ppeRequired; }
    public String getCleaningNotes() { return cleaningNotes; }
    public void setCleaningNotes(String cleaningNotes) { this.cleaningNotes = cleaningNotes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
