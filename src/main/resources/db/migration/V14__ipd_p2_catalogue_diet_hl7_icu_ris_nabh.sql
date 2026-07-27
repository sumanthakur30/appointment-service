-- Wave P2+: ICD catalogue, diet kitchen, HL7 ADT log, visiting hours, ICU devices, RIS DICOM, NABH indicator defs
CREATE TABLE IF NOT EXISTS ipd_code_catalogue (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    system_code VARCHAR(32) NOT NULL,
    code VARCHAR(64) NOT NULL,
    display VARCHAR(512) NOT NULL,
    search_text VARCHAR(1024),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, shop_id, system_code, code)
);
CREATE INDEX IF NOT EXISTS idx_ipd_code_cat_search
    ON ipd_code_catalogue (tenant_id, shop_id, system_code, active);

ALTER TABLE diet_plan
    ADD COLUMN IF NOT EXISTS kitchen_status VARCHAR(32) NOT NULL DEFAULT 'ORDERED',
    ADD COLUMN IF NOT EXISTS tray_acked_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS tray_acked_by VARCHAR(128);

CREATE TABLE IF NOT EXISTS ipd_hl7_message (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    admission_id BIGINT,
    message_type VARCHAR(32) NOT NULL,
    trigger_event VARCHAR(16) NOT NULL,
    control_id VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
    endpoint VARCHAR(512),
    error_detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_ipd_hl7_tenant
    ON ipd_hl7_message (tenant_id, shop_id, created_at DESC);

CREATE TABLE IF NOT EXISTS ipd_visiting_hours_rule (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    ward_category VARCHAR(64),
    day_of_week VARCHAR(16) NOT NULL DEFAULT 'ALL',
    start_time VARCHAR(8) NOT NULL,
    end_time VARCHAR(8) NOT NULL,
    label VARCHAR(128),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS ipd_device_observation (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    admission_id BIGINT NOT NULL,
    device_type VARCHAR(64) NOT NULL,
    mode VARCHAR(64),
    fio2 NUMERIC(5,2),
    peep NUMERIC(5,2),
    tidal_vol INT,
    rate INT,
    notes TEXT,
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    recorded_by VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ipd_device_adm
    ON ipd_device_observation (tenant_id, shop_id, admission_id, recorded_at DESC);

ALTER TABLE radiology_order
    ADD COLUMN IF NOT EXISTS accession_no VARCHAR(64),
    ADD COLUMN IF NOT EXISTS study_instance_uid VARCHAR(128),
    ADD COLUMN IF NOT EXISTS pacs_url VARCHAR(1024);

CREATE TABLE IF NOT EXISTS ipd_quality_indicator_def (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    formula_key VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, shop_id, code)
);
