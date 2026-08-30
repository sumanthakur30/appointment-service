package com.shopmanagement.appointmentservice.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.shopmanagement.appointmentservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "appointments")
public class Appointment extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 30)
    private String type = "SCHEDULED";

    @Column(nullable = false, length = 30)
    private String status = "BOOKED";

    @Column(name = "chief_complaint", length = 1000)
    private String chiefComplaint;

    @Column(name = "consultation_fee")
    private Double consultationFee;

    @Column(name = "payment_type", length = 30)
    private String paymentType;

    @Column(name = "booked_by_account_id")
    private Long bookedByAccountId;

    @Column(name = "booked_via", length = 30)
    private String bookedVia;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "last_edited_by", length = 120)
    private String lastEditedBy;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

    /** Reception display name for the newly assigned doctor (not persisted). */
    @Transient
    private String doctorName;

    /** Catalog follow-up fee for billing quote on doctor change (not persisted). */
    @Transient
    private Double followupFee;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public Long getBookedByAccountId() { return bookedByAccountId; }
    public void setBookedByAccountId(Long bookedByAccountId) { this.bookedByAccountId = bookedByAccountId; }
    public String getBookedVia() { return bookedVia; }
    public void setBookedVia(String bookedVia) { this.bookedVia = bookedVia; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public String getLastEditedBy() { return lastEditedBy; }
    public void setLastEditedBy(String lastEditedBy) { this.lastEditedBy = lastEditedBy; }
    public LocalDateTime getLastEditedAt() { return lastEditedAt; }
    public void setLastEditedAt(LocalDateTime lastEditedAt) { this.lastEditedAt = lastEditedAt; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public Double getFollowupFee() { return followupFee; }
    public void setFollowupFee(Double followupFee) { this.followupFee = followupFee; }
}
