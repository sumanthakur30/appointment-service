package com.shopmanagement.ipdservice.radiology;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "radiology_order")
public class RadiologyOrder extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "encounter_id")
    private Long encounterId;

    @Column(nullable = false, length = 32)
    private String modality = "XRAY";

    @Column(name = "study_code", nullable = false, length = 64)
    private String studyCode;

    @Column(name = "study_name", nullable = false, length = 255)
    private String studyName;

    @Column(nullable = false, length = 32)
    private String status = "ORDERED";

    @Column(name = "clinical_indication", columnDefinition = "TEXT")
    private String clinicalIndication;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt = LocalDateTime.now();

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @Column(name = "report_text", columnDefinition = "TEXT")
    private String reportText;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "accession_no", length = 64)
    private String accessionNo;

    @Column(name = "study_instance_uid", length = 128)
    private String studyInstanceUid;

    @Column(name = "pacs_url", length = 1024)
    private String pacsUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }
    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }
    public String getStudyCode() { return studyCode; }
    public void setStudyCode(String studyCode) { this.studyCode = studyCode; }
    public String getStudyName() { return studyName; }
    public void setStudyName(String studyName) { this.studyName = studyName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getClinicalIndication() { return clinicalIndication; }
    public void setClinicalIndication(String clinicalIndication) { this.clinicalIndication = clinicalIndication; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    public String getReportText() { return reportText; }
    public void setReportText(String reportText) { this.reportText = reportText; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getAccessionNo() { return accessionNo; }
    public void setAccessionNo(String accessionNo) { this.accessionNo = accessionNo; }
    public String getStudyInstanceUid() { return studyInstanceUid; }
    public void setStudyInstanceUid(String studyInstanceUid) { this.studyInstanceUid = studyInstanceUid; }
    public String getPacsUrl() { return pacsUrl; }
    public void setPacsUrl(String pacsUrl) { this.pacsUrl = pacsUrl; }
}
