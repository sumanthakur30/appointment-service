package com.shopmanagement.appointmentservice.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BranchScopedEntity extends TenantScopedEntity {

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
}
