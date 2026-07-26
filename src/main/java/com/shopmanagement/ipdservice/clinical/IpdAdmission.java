package com.shopmanagement.ipdservice.clinical;

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
@Table(name = "ipd_admission")
public class IpdAdmission extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_no", nullable = false, length = 64)
    private String admissionNo;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "patient_name", length = 191)
    private String patientName;

    @Column(name = "encounter_id")
    private Long encounterId;

    @Column(name = "consultant_doctor_id")
    private Long consultantDoctorId;

    @Column(length = 128)
    private String department;

    @Column(length = 512)
    private String diagnosis;

    @Column(name = "admission_reason", columnDefinition = "TEXT")
    private String admissionReason;

    @Column(name = "expected_stay_days")
    private Integer expectedStayDays;

    @Column(name = "ward_preference", length = 128)
    private String wardPreference;

    @Column(nullable = false, length = 32)
    private String priority = "ROUTINE";

    @Column(name = "insurance_ref", length = 128)
    private String insuranceRef;

    @Column(name = "corporate_ref", length = 128)
    private String corporateRef;

    @Column(name = "package_code", length = 64)
    private String packageCode;

    @Column(name = "package_amount", precision = 12, scale = 2)
    private BigDecimal packageAmount;

    @Column(name = "tpa_name", length = 191)
    private String tpaName;

    @Column(name = "tpa_preauth_status", length = 32)
    private String tpaPreauthStatus;

    @Column(name = "tpa_preauth_ref", length = 128)
    private String tpaPreauthRef;

    @Column(name = "tpa_approved_amount", precision = 12, scale = 2)
    private BigDecimal tpaApprovedAmount;

    @Column(name = "tpa_notes", columnDefinition = "TEXT")
    private String tpaNotes;

    @Column(nullable = false)
    private boolean emergency;

    @Column(name = "deposit_amount", precision = 12, scale = 2)
    private BigDecimal depositAmount;

    @Column(nullable = false, length = 32)
    private String status = "REQUESTED";

    @Column(name = "bed_id")
    private Long bedId;

    @Column(name = "occupancy_id")
    private Long occupancyId;

    @Column(name = "admitted_at")
    private LocalDateTime admittedAt;

    @Column(name = "discharged_at")
    private LocalDateTime dischargedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "consent_form_key", length = 128)
    private String consentFormKey;

    @Column(name = "consent_answers_json", columnDefinition = "TEXT")
    private String consentAnswersJson;

    @Column(name = "consent_captured_at")
    private LocalDateTime consentCapturedAt;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAdmissionNo() { return admissionNo; }
    public void setAdmissionNo(String admissionNo) { this.admissionNo = admissionNo; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }
    public Long getConsultantDoctorId() { return consultantDoctorId; }
    public void setConsultantDoctorId(Long consultantDoctorId) { this.consultantDoctorId = consultantDoctorId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getAdmissionReason() { return admissionReason; }
    public void setAdmissionReason(String admissionReason) { this.admissionReason = admissionReason; }
    public Integer getExpectedStayDays() { return expectedStayDays; }
    public void setExpectedStayDays(Integer expectedStayDays) { this.expectedStayDays = expectedStayDays; }
    public String getWardPreference() { return wardPreference; }
    public void setWardPreference(String wardPreference) { this.wardPreference = wardPreference; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getInsuranceRef() { return insuranceRef; }
    public void setInsuranceRef(String insuranceRef) { this.insuranceRef = insuranceRef; }
    public String getCorporateRef() { return corporateRef; }
    public void setCorporateRef(String corporateRef) { this.corporateRef = corporateRef; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public BigDecimal getPackageAmount() { return packageAmount; }
    public void setPackageAmount(BigDecimal packageAmount) { this.packageAmount = packageAmount; }
    public String getTpaName() { return tpaName; }
    public void setTpaName(String tpaName) { this.tpaName = tpaName; }
    public String getTpaPreauthStatus() { return tpaPreauthStatus; }
    public void setTpaPreauthStatus(String tpaPreauthStatus) { this.tpaPreauthStatus = tpaPreauthStatus; }
    public String getTpaPreauthRef() { return tpaPreauthRef; }
    public void setTpaPreauthRef(String tpaPreauthRef) { this.tpaPreauthRef = tpaPreauthRef; }
    public BigDecimal getTpaApprovedAmount() { return tpaApprovedAmount; }
    public void setTpaApprovedAmount(BigDecimal tpaApprovedAmount) { this.tpaApprovedAmount = tpaApprovedAmount; }
    public String getTpaNotes() { return tpaNotes; }
    public void setTpaNotes(String tpaNotes) { this.tpaNotes = tpaNotes; }
    public boolean isEmergency() { return emergency; }
    public void setEmergency(boolean emergency) { this.emergency = emergency; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getBedId() { return bedId; }
    public void setBedId(Long bedId) { this.bedId = bedId; }
    public Long getOccupancyId() { return occupancyId; }
    public void setOccupancyId(Long occupancyId) { this.occupancyId = occupancyId; }
    public LocalDateTime getAdmittedAt() { return admittedAt; }
    public void setAdmittedAt(LocalDateTime admittedAt) { this.admittedAt = admittedAt; }
    public LocalDateTime getDischargedAt() { return dischargedAt; }
    public void setDischargedAt(LocalDateTime dischargedAt) { this.dischargedAt = dischargedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getConsentFormKey() { return consentFormKey; }
    public void setConsentFormKey(String consentFormKey) { this.consentFormKey = consentFormKey; }
    public String getConsentAnswersJson() { return consentAnswersJson; }
    public void setConsentAnswersJson(String consentAnswersJson) { this.consentAnswersJson = consentAnswersJson; }
    public LocalDateTime getConsentCapturedAt() { return consentCapturedAt; }
    public void setConsentCapturedAt(LocalDateTime consentCapturedAt) { this.consentCapturedAt = consentCapturedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
