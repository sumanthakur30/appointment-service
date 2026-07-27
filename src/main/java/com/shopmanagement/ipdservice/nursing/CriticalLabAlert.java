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
@Table(name = "ipd_critical_lab_alert")
public class CriticalLabAlert extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "lab_order_id")
    private Long labOrderId;

    @Column(name = "lab_result_id", length = 128)
    private String labResultId;

    @Column(name = "parameter_name", length = 191)
    private String parameterName;

    @Column(name = "result_value", length = 128)
    private String resultValue;

    @Column(nullable = false, length = 32)
    private String flag = "CRITICAL";

    @Column(nullable = false, length = 32)
    private String status = "OPEN";

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "acked_at")
    private LocalDateTime ackedAt;

    @Column(name = "acked_by", length = 128)
    private String ackedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getLabOrderId() { return labOrderId; }
    public void setLabOrderId(Long labOrderId) { this.labOrderId = labOrderId; }
    public String getLabResultId() { return labResultId; }
    public void setLabResultId(String labResultId) { this.labResultId = labResultId; }
    public String getParameterName() { return parameterName; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }
    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }
    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public LocalDateTime getAckedAt() { return ackedAt; }
    public void setAckedAt(LocalDateTime ackedAt) { this.ackedAt = ackedAt; }
    public String getAckedBy() { return ackedBy; }
    public void setAckedBy(String ackedBy) { this.ackedBy = ackedBy; }
}
