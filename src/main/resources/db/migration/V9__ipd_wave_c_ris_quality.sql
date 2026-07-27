-- Wave C: RIS stubs + quality incidents
CREATE TABLE IF NOT EXISTS radiology_order (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    admission_id BIGINT NOT NULL REFERENCES ipd_admission(id),
    patient_id BIGINT NOT NULL,
    encounter_id BIGINT,
    modality VARCHAR(32) NOT NULL DEFAULT 'XRAY',
    study_code VARCHAR(64) NOT NULL,
    study_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ORDERED',
    clinical_indication TEXT,
    ordered_at TIMESTAMP NOT NULL DEFAULT NOW(),
    reported_at TIMESTAMP,
    report_text TEXT,
    created_by VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_radiology_order_admission
    ON radiology_order (tenant_id, shop_id, admission_id, ordered_at DESC);

CREATE TABLE IF NOT EXISTS quality_incident (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    admission_id BIGINT,
    incident_type VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    nabh_indicator_code VARCHAR(64),
    capa_notes TEXT,
    reported_by VARCHAR(128),
    reported_at TIMESTAMP NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quality_incident_shop
    ON quality_incident (tenant_id, shop_id, status, reported_at DESC);
