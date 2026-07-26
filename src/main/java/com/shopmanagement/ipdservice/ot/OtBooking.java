package com.shopmanagement.ipdservice.ot;

import java.time.LocalDateTime;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ot_booking")
public class OtBooking extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "booking_no", nullable = false, length = 64)
    private String bookingNo;

    @Column(name = "theatre_code", length = 64)
    private String theatreCode;

    @Column(name = "theatre_name", length = 191)
    private String theatreName;

    @Column(name = "procedure_name", nullable = false, length = 255)
    private String procedureName;

    @Column(name = "surgeon_name", length = 191)
    private String surgeonName;

    @Column(length = 191)
    private String anaesthetist;

    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end")
    private LocalDateTime scheduledEnd;

    /** REQUESTED | BOOKED | PRE_OP | IN_PROGRESS | RECOVERY | COMPLETED | CANCELLED */
    @Column(nullable = false, length = 32)
    private String status = "REQUESTED";

    @Column(name = "preop_notes", columnDefinition = "TEXT")
    private String preopNotes;

    @Column(name = "postop_notes", columnDefinition = "TEXT")
    private String postopNotes;

    @Column(name = "recovery_bed_id")
    private Long recoveryBedId;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getBookingNo() { return bookingNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }
    public String getTheatreCode() { return theatreCode; }
    public void setTheatreCode(String theatreCode) { this.theatreCode = theatreCode; }
    public String getTheatreName() { return theatreName; }
    public void setTheatreName(String theatreName) { this.theatreName = theatreName; }
    public String getProcedureName() { return procedureName; }
    public void setProcedureName(String procedureName) { this.procedureName = procedureName; }
    public String getSurgeonName() { return surgeonName; }
    public void setSurgeonName(String surgeonName) { this.surgeonName = surgeonName; }
    public String getAnaesthetist() { return anaesthetist; }
    public void setAnaesthetist(String anaesthetist) { this.anaesthetist = anaesthetist; }
    public LocalDateTime getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(LocalDateTime scheduledStart) { this.scheduledStart = scheduledStart; }
    public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(LocalDateTime scheduledEnd) { this.scheduledEnd = scheduledEnd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPreopNotes() { return preopNotes; }
    public void setPreopNotes(String preopNotes) { this.preopNotes = preopNotes; }
    public String getPostopNotes() { return postopNotes; }
    public void setPostopNotes(String postopNotes) { this.postopNotes = postopNotes; }
    public Long getRecoveryBedId() { return recoveryBedId; }
    public void setRecoveryBedId(Long recoveryBedId) { this.recoveryBedId = recoveryBedId; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
