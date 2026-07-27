package com.shopmanagement.ipdservice.family;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ipd_visiting_hours_rule")
public class VisitingHoursRule extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ward_category", length = 64)
    private String wardCategory;

    @Column(name = "day_of_week", nullable = false, length = 16)
    private String dayOfWeek = "ALL";

    @Column(name = "start_time", nullable = false, length = 8)
    private String startTime;

    @Column(name = "end_time", nullable = false, length = 8)
    private String endTime;

    @Column(length = 128)
    private String label;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWardCategory() { return wardCategory; }
    public void setWardCategory(String wardCategory) { this.wardCategory = wardCategory; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
