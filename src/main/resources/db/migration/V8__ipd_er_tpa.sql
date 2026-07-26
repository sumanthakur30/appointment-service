-- Wave B4/B5: ER triage + TPA pre-auth stubs
CREATE TABLE IF NOT EXISTS er_triage (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(191),
    acuity VARCHAR(16) NOT NULL DEFAULT 'ESI3',
    chief_complaint TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'WAITING',
    arrival_at TIMESTAMP NOT NULL DEFAULT NOW(),
    linked_admission_id BIGINT,
    notes TEXT,
    created_by VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_er_triage_active
    ON er_triage (tenant_id, shop_id, status, arrival_at DESC);

ALTER TABLE ipd_admission
    ADD COLUMN IF NOT EXISTS tpa_name VARCHAR(191),
    ADD COLUMN IF NOT EXISTS tpa_preauth_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS tpa_preauth_ref VARCHAR(128),
    ADD COLUMN IF NOT EXISTS tpa_approved_amount NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS tpa_notes TEXT;
