package com.shopmanagement.ipdservice.ot;

import com.shopmanagement.ipdservice.model.base.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ot_preference_card")
public class OtPreferenceCard extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(name = "surgeon_name", length = 191)
    private String surgeonName;

    @Column(name = "procedure_code", length = 64)
    private String procedureCode;

    @Column(name = "procedure_name", nullable = false, length = 255)
    private String procedureName;

    @Column(name = "instruments_json", columnDefinition = "TEXT")
    private String instrumentsJson;

    @Column(name = "implants_json", columnDefinition = "TEXT")
    private String implantsJson;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getSurgeonName() { return surgeonName; }
    public void setSurgeonName(String surgeonName) { this.surgeonName = surgeonName; }
    public String getProcedureCode() { return procedureCode; }
    public void setProcedureCode(String procedureCode) { this.procedureCode = procedureCode; }
    public String getProcedureName() { return procedureName; }
    public void setProcedureName(String procedureName) { this.procedureName = procedureName; }
    public String getInstrumentsJson() { return instrumentsJson; }
    public void setInstrumentsJson(String instrumentsJson) { this.instrumentsJson = instrumentsJson; }
    public String getImplantsJson() { return implantsJson; }
    public void setImplantsJson(String implantsJson) { this.implantsJson = implantsJson; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
