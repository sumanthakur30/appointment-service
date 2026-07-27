-- Wave P1b: handover, critical labs, transfer checklist, CPOE sets, OT prefs/implants, HAI registry
ALTER TABLE infection_isolation
    ADD COLUMN IF NOT EXISTS reason_code VARCHAR(64);

ALTER TABLE ipd_transfer
    ADD COLUMN IF NOT EXISTS checklist_submission_id BIGINT;

CREATE TABLE IF NOT EXISTS ipd_critical_lab_alert (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT,
    lab_order_id BIGINT,
    lab_result_id VARCHAR(128),
    parameter_name VARCHAR(191),
    result_value VARCHAR(128),
    flag VARCHAR(32) NOT NULL DEFAULT 'CRITICAL',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    acked_at TIMESTAMP,
    acked_by VARCHAR(128)
);
CREATE INDEX IF NOT EXISTS idx_ipd_crit_lab_tenant_status
    ON ipd_critical_lab_alert (tenant_id, shop_id, status, created_at DESC);

CREATE TABLE IF NOT EXISTS cpoe_order_set (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(191) NOT NULL,
    specialty VARCHAR(128),
    definition_json TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, shop_id, code)
);

CREATE TABLE IF NOT EXISTS ot_preference_card (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    surgeon_name VARCHAR(191),
    procedure_code VARCHAR(64),
    procedure_name VARCHAR(255) NOT NULL,
    instruments_json TEXT,
    implants_json TEXT,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, shop_id, code)
);

CREATE TABLE IF NOT EXISTS ot_implant_usage (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    ot_booking_id BIGINT NOT NULL,
    implant_sku VARCHAR(128) NOT NULL,
    implant_name VARCHAR(255),
    lot_number VARCHAR(128),
    quantity INT NOT NULL DEFAULT 1,
    laterality VARCHAR(32),
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    recorded_by VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ot_implant_booking
    ON ot_implant_usage (tenant_id, shop_id, ot_booking_id);

CREATE TABLE IF NOT EXISTS hai_event (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    admission_id BIGINT NOT NULL,
    hai_type VARCHAR(64) NOT NULL,
    onset_date DATE,
    device_type VARCHAR(128),
    isolation_id BIGINT,
    quality_incident_id BIGINT,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(128),
    closed_at TIMESTAMP,
    closed_by VARCHAR(128)
);
CREATE INDEX IF NOT EXISTS idx_hai_tenant_status
    ON hai_event (tenant_id, shop_id, status, created_at DESC);
