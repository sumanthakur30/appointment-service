package com.shopmanagement.ipdservice.diet;

import java.time.LocalDate;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "diet_plan")
public class DietPlan extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    /** REGULAR | DIABETIC | RENAL | SOFT | NPO | CUSTOM */
    @Column(name = "diet_type", nullable = false, length = 64)
    private String dietType = "REGULAR";

    @Column(name = "fluid_restriction_ml")
    private Integer fluidRestrictionMl;

    @Column(length = 512)
    private String breakfast;

    @Column(length = 512)
    private String lunch;

    @Column(length = 512)
    private String dinner;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 128)
    private String dietician;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom = LocalDate.now();

    /** ORDERED | PREPARING | READY | SERVED | CANCELLED */
    @Column(name = "kitchen_status", nullable = false, length = 32)
    private String kitchenStatus = "ORDERED";

    @Column(name = "tray_acked_at")
    private java.time.LocalDateTime trayAckedAt;

    @Column(name = "tray_acked_by", length = 128)
    private String trayAckedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getDietType() { return dietType; }
    public void setDietType(String dietType) { this.dietType = dietType; }
    public Integer getFluidRestrictionMl() { return fluidRestrictionMl; }
    public void setFluidRestrictionMl(Integer fluidRestrictionMl) { this.fluidRestrictionMl = fluidRestrictionMl; }
    public String getBreakfast() { return breakfast; }
    public void setBreakfast(String breakfast) { this.breakfast = breakfast; }
    public String getLunch() { return lunch; }
    public void setLunch(String lunch) { this.lunch = lunch; }
    public String getDinner() { return dinner; }
    public void setDinner(String dinner) { this.dinner = dinner; }
    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getDietician() { return dietician; }
    public void setDietician(String dietician) { this.dietician = dietician; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public String getKitchenStatus() { return kitchenStatus; }
    public void setKitchenStatus(String kitchenStatus) { this.kitchenStatus = kitchenStatus; }
    public java.time.LocalDateTime getTrayAckedAt() { return trayAckedAt; }
    public void setTrayAckedAt(java.time.LocalDateTime trayAckedAt) { this.trayAckedAt = trayAckedAt; }
    public String getTrayAckedBy() { return trayAckedBy; }
    public void setTrayAckedBy(String trayAckedBy) { this.trayAckedBy = trayAckedBy; }
}
