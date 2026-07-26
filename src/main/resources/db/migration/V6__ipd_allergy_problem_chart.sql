-- Wave A4: patient-scoped allergy + problem list (tenant/shop), reusable across admissions.
CREATE TABLE IF NOT EXISTS ipd_patient_allergy (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    patient_id BIGINT NOT NULL,
    substance VARCHAR(255) NOT NULL,
    reaction VARCHAR(512),
    severity VARCHAR(32) NOT NULL DEFAULT 'MODERATE',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    noted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    recorded_by VARCHAR(128),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ipd_allergy_patient
    ON ipd_patient_allergy (tenant_id, shop_id, patient_id);

CREATE TABLE IF NOT EXISTS ipd_patient_problem (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    patient_id BIGINT NOT NULL,
    problem VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    onset_date DATE,
    recorded_by VARCHAR(128),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ipd_problem_patient
    ON ipd_patient_problem (tenant_id, shop_id, patient_id);
