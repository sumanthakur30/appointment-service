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
@Table(name = "cssd_cycle")
public class CssdCycle extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "set_id", nullable = false)
    private Long setId;

    @Column(name = "cycle_type", nullable = false, length = 32)
    private String cycleType = "STEAM";

    @Column(nullable = false, length = 32)
    private String status = "STARTED";

    @Column(name = "autoclave_ref", length = 64)
    private String autoclaveRef;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "performed_by", length = 128)
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSetId() { return setId; }
    public void setSetId(Long setId) { this.setId = setId; }
    public String getCycleType() { return cycleType; }
    public void setCycleType(String cycleType) { this.cycleType = cycleType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAutoclaveRef() { return autoclaveRef; }
    public void setAutoclaveRef(String autoclaveRef) { this.autoclaveRef = autoclaveRef; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
