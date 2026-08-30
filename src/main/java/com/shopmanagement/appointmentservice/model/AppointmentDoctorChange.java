package com.shopmanagement.appointmentservice.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointment_doctor_changes")
public class AppointmentDoctorChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "shop_id", nullable = false, length = 100)
    private String shopId;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "previous_doctor_id", nullable = false)
    private Long previousDoctorId;

    @Column(name = "new_doctor_id", nullable = false)
    private Long newDoctorId;

    @Column(name = "previous_consultation_fee")
    private Double previousConsultationFee;

    @Column(name = "new_consultation_fee")
    private Double newConsultationFee;

    @Column(name = "changed_by", nullable = false, length = 120)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getPreviousDoctorId() { return previousDoctorId; }
    public void setPreviousDoctorId(Long previousDoctorId) { this.previousDoctorId = previousDoctorId; }
    public Long getNewDoctorId() { return newDoctorId; }
    public void setNewDoctorId(Long newDoctorId) { this.newDoctorId = newDoctorId; }
    public Double getPreviousConsultationFee() { return previousConsultationFee; }
    public void setPreviousConsultationFee(Double previousConsultationFee) { this.previousConsultationFee = previousConsultationFee; }
    public Double getNewConsultationFee() { return newConsultationFee; }
    public void setNewConsultationFee(Double newConsultationFee) { this.newConsultationFee = newConsultationFee; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
