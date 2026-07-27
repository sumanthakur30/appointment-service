package com.shopmanagement.ipdservice.icu;

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
@Table(name = "ipd_device_observation")
public class DeviceObservation extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    /** VENTILATOR | BIPAP | MONITOR | INFUSION | OTHER */
    @Column(name = "device_type", nullable = false, length = 64)
    private String deviceType;

    @Column(length = 64)
    private String mode;

    @Column(precision = 5, scale = 2)
    private BigDecimal fio2;

    @Column(precision = 5, scale = 2)
    private BigDecimal peep;

    @Column(name = "tidal_vol")
    private Integer tidalVol;

    private Integer rate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "recorded_by", length = 128)
    private String recordedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public BigDecimal getFio2() { return fio2; }
    public void setFio2(BigDecimal fio2) { this.fio2 = fio2; }
    public BigDecimal getPeep() { return peep; }
    public void setPeep(BigDecimal peep) { this.peep = peep; }
    public Integer getTidalVol() { return tidalVol; }
    public void setTidalVol(Integer tidalVol) { this.tidalVol = tidalVol; }
    public Integer getRate() { return rate; }
    public void setRate(Integer rate) { this.rate = rate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}
