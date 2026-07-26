-- Accommodation engine + IPD clinical (Phase 1)
-- Reuses hostel bed/occupancy semantics with richer hierarchy + clinical FKs.

CREATE TABLE IF NOT EXISTS accommodation_profile (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    accommodation_type VARCHAR(64) NOT NULL DEFAULT 'HOSPITAL_IPD',
    glossary_json   TEXT NOT NULL DEFAULT '{}',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_accommodation_profile_tenant_shop UNIQUE (tenant_id, shop_id)
);

CREATE TABLE IF NOT EXISTS accommodation_node (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    parent_id       BIGINT NULL REFERENCES accommodation_node(id),
    node_type       VARCHAR(32) NOT NULL,
    code            VARCHAR(64) NOT NULL,
    name            VARCHAR(191) NOT NULL,
    category        VARCHAR(64),
    metadata_json   TEXT,
    sort_order      INT NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_accommodation_node_code UNIQUE (tenant_id, shop_id, code)
);

CREATE INDEX IF NOT EXISTS idx_accommodation_node_parent
    ON accommodation_node (tenant_id, shop_id, parent_id);
CREATE INDEX IF NOT EXISTS idx_accommodation_node_type
    ON accommodation_node (tenant_id, shop_id, node_type);

CREATE TABLE IF NOT EXISTS accommodation_bed (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    room_node_id    BIGINT NOT NULL REFERENCES accommodation_node(id),
    bed_code        VARCHAR(64) NOT NULL,
    bed_no          INT NOT NULL DEFAULT 1,
    category        VARCHAR(64),
    status          VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    isolation_flag  BOOLEAN NOT NULL DEFAULT FALSE,
    daily_charge    NUMERIC(12, 2),
    metadata_json   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_accommodation_bed_code UNIQUE (tenant_id, shop_id, bed_code)
);

CREATE INDEX IF NOT EXISTS idx_accommodation_bed_room
    ON accommodation_bed (tenant_id, shop_id, room_node_id);
CREATE INDEX IF NOT EXISTS idx_accommodation_bed_status
    ON accommodation_bed (tenant_id, shop_id, status);

CREATE TABLE IF NOT EXISTS bed_occupancy (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    bed_id          BIGINT NOT NULL REFERENCES accommodation_bed(id),
    occupant_ref    VARCHAR(64),
    occupant_name   VARCHAR(191),
    admission_no    VARCHAR(64) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    allocated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    expected_discharge_at TIMESTAMP,
    released_at     TIMESTAMP,
    nurse_staff_id  BIGINT,
    created_by      VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_bed_occupancy_bed
    ON bed_occupancy (tenant_id, shop_id, bed_id, status);
CREATE INDEX IF NOT EXISTS idx_bed_occupancy_admission
    ON bed_occupancy (tenant_id, shop_id, admission_no);

CREATE TABLE IF NOT EXISTS ipd_admission (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_no    VARCHAR(64) NOT NULL,
    patient_id      BIGINT NOT NULL,
    patient_name    VARCHAR(191),
    encounter_id    BIGINT,
    consultant_doctor_id BIGINT,
    department      VARCHAR(128),
    diagnosis       VARCHAR(512),
    admission_reason TEXT,
    expected_stay_days INT,
    ward_preference VARCHAR(128),
    priority        VARCHAR(32) NOT NULL DEFAULT 'ROUTINE',
    insurance_ref   VARCHAR(128),
    corporate_ref   VARCHAR(128),
    package_code    VARCHAR(64),
    emergency       BOOLEAN NOT NULL DEFAULT FALSE,
    deposit_amount  NUMERIC(12, 2),
    status          VARCHAR(32) NOT NULL DEFAULT 'REQUESTED',
    bed_id          BIGINT REFERENCES accommodation_bed(id),
    occupancy_id    BIGINT REFERENCES bed_occupancy(id),
    admitted_at     TIMESTAMP,
    discharged_at   TIMESTAMP,
    notes           TEXT,
    created_by      VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ipd_admission_no UNIQUE (tenant_id, shop_id, admission_no)
);

CREATE INDEX IF NOT EXISTS idx_ipd_admission_patient
    ON ipd_admission (tenant_id, shop_id, patient_id);
CREATE INDEX IF NOT EXISTS idx_ipd_admission_status
    ON ipd_admission (tenant_id, shop_id, status);

CREATE TABLE IF NOT EXISTS ipd_transfer (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    from_bed_id     BIGINT NOT NULL,
    to_bed_id       BIGINT NOT NULL,
    reason          VARCHAR(512),
    status          VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    transferred_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    approved_by     VARCHAR(128),
    created_by      VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ipd_transfer_admission
    ON ipd_transfer (tenant_id, shop_id, admission_id);
