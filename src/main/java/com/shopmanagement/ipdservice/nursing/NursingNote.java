package com.shopmanagement.ipdservice.nursing;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nursing_note")
public class NursingNote extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    /** PROGRESS | DAILY_ASSESSMENT | PRESSURE_ULCER | HANDOVER | ALERT */
    @Column(name = "note_type", nullable = false, length = 64)
    private String noteType = "PROGRESS";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "assessment_json", columnDefinition = "TEXT")
    private String assessmentJson;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "recorded_by", length = 128)
    private String recordedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getNoteType() { return noteType; }
    public void setNoteType(String noteType) { this.noteType = noteType; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getAssessmentJson() { return assessmentJson; }
    public void setAssessmentJson(String assessmentJson) { this.assessmentJson = assessmentJson; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}
