package com.shopmanagement.ipdservice.family;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "visitor_pass")
public class VisitorPass extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "pass_code", nullable = false, length = 32)
    private String passCode;

    @Column(name = "visitor_name", nullable = false, length = 191)
    private String visitorName;

    @Column(length = 64)
    private String relation;

    @Column(length = 32)
    private String phone;

    @Column(name = "visiting_hours", length = 128)
    private String visitingHours;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom = LocalDateTime.now();

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    /** ACTIVE | REVOKED | EXPIRED */
    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_by", length = 128)
    private String createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getPassCode() { return passCode; }
    public void setPassCode(String passCode) { this.passCode = passCode; }
    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getVisitingHours() { return visitingHours; }
    public void setVisitingHours(String visitingHours) { this.visitingHours = visitingHours; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
