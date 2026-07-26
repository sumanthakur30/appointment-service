package com.shopmanagement.ipdservice.billing;

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
@Table(name = "ipd_bill")
public class IpdBill extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "admission_no", nullable = false, length = 64)
    private String admissionNo;

    @Column(name = "bill_type", nullable = false, length = 16)
    private String billType;

    @Column(name = "package_code", length = 64)
    private String packageCode;

    @Column(name = "package_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal packageAmount = BigDecimal.ZERO;

    @Column(name = "charge_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal chargeTotal = BigDecimal.ZERO;

    @Column(name = "deposit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "gross_payable", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossPayable = BigDecimal.ZERO;

    @Column(name = "amount_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountDue = BigDecimal.ZERO;

    @Column(nullable = false, length = 32)
    private String status = "FINALIZED";

    @Column(name = "lines_json", columnDefinition = "TEXT")
    private String linesJson;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @Column(name = "finalized_by", length = 128)
    private String finalizedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdmissionId() { return admissionId; }
    public void setAdmissionId(Long admissionId) { this.admissionId = admissionId; }
    public String getAdmissionNo() { return admissionNo; }
    public void setAdmissionNo(String admissionNo) { this.admissionNo = admissionNo; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public BigDecimal getPackageAmount() { return packageAmount; }
    public void setPackageAmount(BigDecimal packageAmount) { this.packageAmount = packageAmount; }
    public BigDecimal getChargeTotal() { return chargeTotal; }
    public void setChargeTotal(BigDecimal chargeTotal) { this.chargeTotal = chargeTotal; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public BigDecimal getGrossPayable() { return grossPayable; }
    public void setGrossPayable(BigDecimal grossPayable) { this.grossPayable = grossPayable; }
    public BigDecimal getAmountDue() { return amountDue; }
    public void setAmountDue(BigDecimal amountDue) { this.amountDue = amountDue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLinesJson() { return linesJson; }
    public void setLinesJson(String linesJson) { this.linesJson = linesJson; }
    public LocalDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(LocalDateTime finalizedAt) { this.finalizedAt = finalizedAt; }
    public String getFinalizedBy() { return finalizedBy; }
    public void setFinalizedBy(String finalizedBy) { this.finalizedBy = finalizedBy; }
}
