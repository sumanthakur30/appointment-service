package com.shopmanagement.ipdservice.nursing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nursing_vital")
public class NursingVital extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "temperature_c", precision = 5, scale = 2)
    private BigDecimal temperatureC;

    @Column(name = "pulse_bpm")
    private Integer pulseBpm;

    @Column(name = "resp_rate")
    private Integer respRate;

    @Column(name = "bp_systolic")
    private Integer bpSystolic;

    @Column(name = "bp_diastolic")
    private Integer bpDiastolic;

    @Column(precision = 5, scale = 2)
    private BigDecimal spo2;

    @Column(name = "pain_score")
    private Integer painScore;

    @Column(name = "fall_risk", length = 32)
    private String fallRisk;

    @Column(name = "recorded_by", length = 128)
    private String recordedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public BigDecimal getTemperatureC() { return temperatureC; }
    public void setTemperatureC(BigDecimal temperatureC) { this.temperatureC = temperatureC; }
    public Integer getPulseBpm() { return pulseBpm; }
    public void setPulseBpm(Integer pulseBpm) { this.pulseBpm = pulseBpm; }
    public Integer getRespRate() { return respRate; }
    public void setRespRate(Integer respRate) { this.respRate = respRate; }
    public Integer getBpSystolic() { return bpSystolic; }
    public void setBpSystolic(Integer bpSystolic) { this.bpSystolic = bpSystolic; }
    public Integer getBpDiastolic() { return bpDiastolic; }
    public void setBpDiastolic(Integer bpDiastolic) { this.bpDiastolic = bpDiastolic; }
    public BigDecimal getSpo2() { return spo2; }
    public void setSpo2(BigDecimal spo2) { this.spo2 = spo2; }
    public Integer getPainScore() { return painScore; }
    public void setPainScore(Integer painScore) { this.painScore = painScore; }
    public String getFallRisk() { return fallRisk; }
    public void setFallRisk(String fallRisk) { this.fallRisk = fallRisk; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
