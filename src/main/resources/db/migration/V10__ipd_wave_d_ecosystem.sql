-- Wave D: FHIR export history, ABHA links, blood bank stubs, CSSD stubs

CREATE TABLE IF NOT EXISTS ipd_fhir_export (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    admission_id BIGINT NOT NULL REFERENCES ipd_admission(id),
    patient_id BIGINT NOT NULL,
    resource_type VARCHAR(64) NOT NULL DEFAULT 'Bundle',
    fhir_json TEXT NOT NULL,
    exported_by VARCHAR(128),
    exported_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ipd_fhir_export_admission
    ON ipd_fhir_export (tenant_id, shop_id, admission_id, exported_at DESC);

CREATE TABLE IF NOT EXISTS ipd_patient_abha (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    patient_id BIGINT NOT NULL,
    abha_number VARCHAR(14) NOT NULL,
    abha_address VARCHAR(191),
    consent_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    consent_at TIMESTAMP,
    ndhm_txn_id VARCHAR(128),
    ndhm_mode VARCHAR(32),
    notes TEXT,
    linked_by VARCHAR(128),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ipd_patient_abha UNIQUE (tenant_id, shop_id, patient_id)
);

CREATE INDEX IF NOT EXISTS idx_ipd_patient_abha_number
    ON ipd_patient_abha (tenant_id, shop_id, abha_number);

CREATE TABLE IF NOT EXISTS blood_unit (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    unit_code VARCHAR(64) NOT NULL,
    blood_group VARCHAR(8) NOT NULL,
    component VARCHAR(32) NOT NULL DEFAULT 'PRBC',
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    collected_at TIMESTAMP,
    expires_at TIMESTAMP,
    donor_ref VARCHAR(128),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_blood_unit_code UNIQUE (tenant_id, shop_id, unit_code)
);

CREATE INDEX IF NOT EXISTS idx_blood_unit_avail
    ON blood_unit (tenant_id, shop_id, blood_group, status);

CREATE TABLE IF NOT EXISTS blood_request (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    admission_id BIGINT REFERENCES ipd_admission(id),
    ot_booking_id BIGINT,
    patient_id BIGINT NOT NULL,
    blood_group VARCHAR(8) NOT NULL,
    component VARCHAR(32) NOT NULL DEFAULT 'PRBC',
    units_requested INT NOT NULL DEFAULT 1,
    clinical_indication TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'REQUESTED',
    matched_unit_id BIGINT REFERENCES blood_unit(id),
    requested_by VARCHAR(128),
    requested_at TIMESTAMP NOT NULL DEFAULT NOW(),
    crossmatched_at TIMESTAMP,
    issued_at TIMESTAMP,
    returned_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_blood_request_admission
    ON blood_request (tenant_id, shop_id, admission_id, requested_at DESC);

CREATE TABLE IF NOT EXISTS cssd_set (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    set_code VARCHAR(64) NOT NULL,
    set_name VARCHAR(191) NOT NULL,
    specialty VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    last_sterilized_at TIMESTAMP,
    issued_ot_booking_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cssd_set_code UNIQUE (tenant_id, shop_id, set_code)
);

CREATE INDEX IF NOT EXISTS idx_cssd_set_status
    ON cssd_set (tenant_id, shop_id, status);

CREATE TABLE IF NOT EXISTS cssd_cycle (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    set_id BIGINT NOT NULL REFERENCES cssd_set(id),
    cycle_type VARCHAR(32) NOT NULL DEFAULT 'STEAM',
    status VARCHAR(32) NOT NULL DEFAULT 'STARTED',
    autoclave_ref VARCHAR(64),
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP,
    performed_by VARCHAR(128),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cssd_cycle_set
    ON cssd_cycle (tenant_id, shop_id, set_id, started_at DESC);
