package com.shopmanagement.ipdservice.fhir;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ipd_fhir_export")
public class IpdFhirExport extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType = "Bundle";

    @Column(name = "fhir_json", nullable = false, columnDefinition = "TEXT")
    private String fhirJson;

    @Column(name = "exported_by", length = 128)
    private String exportedBy;

    @Column(name = "exported_at", nullable = false)
    private LocalDateTime exportedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getFhirJson() { return fhirJson; }
    public void setFhirJson(String fhirJson) { this.fhirJson = fhirJson; }
    public String getExportedBy() { return exportedBy; }
    public void setExportedBy(String exportedBy) { this.exportedBy = exportedBy; }
    public LocalDateTime getExportedAt() { return exportedAt; }
    public void setExportedAt(LocalDateTime exportedAt) { this.exportedAt = exportedAt; }
}
