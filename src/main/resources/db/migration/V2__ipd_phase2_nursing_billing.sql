-- Phase 2: Nursing, MAR, Diet, Daily charge ledger

CREATE TABLE IF NOT EXISTS nursing_vital (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    recorded_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    temperature_c   NUMERIC(5, 2),
    pulse_bpm       INT,
    resp_rate       INT,
    bp_systolic     INT,
    bp_diastolic    INT,
    spo2            NUMERIC(5, 2),
    pain_score      INT,
    fall_risk       VARCHAR(32),
    recorded_by     VARCHAR(128),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_nursing_vital_admission
    ON nursing_vital (tenant_id, shop_id, admission_id, recorded_at DESC);

CREATE TABLE IF NOT EXISTS nursing_intake_output (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    recorded_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    io_type         VARCHAR(16) NOT NULL,
    category        VARCHAR(64),
    amount_ml       NUMERIC(10, 2) NOT NULL,
    recorded_by     VARCHAR(128),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_nursing_io_admission
    ON nursing_intake_output (tenant_id, shop_id, admission_id, recorded_at DESC);

CREATE TABLE IF NOT EXISTS nursing_note (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    note_type       VARCHAR(64) NOT NULL DEFAULT 'PROGRESS',
    body            TEXT NOT NULL,
    assessment_json TEXT,
    recorded_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    recorded_by     VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_nursing_note_admission
    ON nursing_note (tenant_id, shop_id, admission_id, recorded_at DESC);

CREATE TABLE IF NOT EXISTS mar_order (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    medicine_name   VARCHAR(255) NOT NULL,
    dose            VARCHAR(64),
    route           VARCHAR(32),
    frequency       VARCHAR(64),
    schedule_times  VARCHAR(255),
    start_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    end_at          TIMESTAMP,
    status          VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ordered_by      VARCHAR(128),
    barcode         VARCHAR(128),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mar_order_admission
    ON mar_order (tenant_id, shop_id, admission_id, status);

CREATE TABLE IF NOT EXISTS mar_administration (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    mar_order_id    BIGINT NOT NULL REFERENCES mar_order(id),
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    scheduled_at    TIMESTAMP,
    administered_at TIMESTAMP,
    dose_given      VARCHAR(64),
    status          VARCHAR(32) NOT NULL,
    nurse_id        VARCHAR(128),
    reason          VARCHAR(512),
    barcode_scanned VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mar_admin_admission
    ON mar_administration (tenant_id, shop_id, admission_id, administered_at DESC);
CREATE INDEX IF NOT EXISTS idx_mar_admin_order
    ON mar_administration (tenant_id, shop_id, mar_order_id);

CREATE TABLE IF NOT EXISTS diet_plan (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    diet_type       VARCHAR(64) NOT NULL DEFAULT 'REGULAR',
    fluid_restriction_ml INT,
    breakfast       VARCHAR(512),
    lunch           VARCHAR(512),
    dinner          VARCHAR(512),
    special_notes   TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    dietician       VARCHAR(128),
    effective_from  DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_diet_plan_admission
    ON diet_plan (tenant_id, shop_id, admission_id, active);

CREATE TABLE IF NOT EXISTS ipd_charge_line (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    admission_no    VARCHAR(64) NOT NULL,
    charge_date     DATE NOT NULL,
    charge_type     VARCHAR(64) NOT NULL,
    description     VARCHAR(512),
    quantity        NUMERIC(12, 2) NOT NULL DEFAULT 1,
    unit_amount     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    amount          NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status          VARCHAR(32) NOT NULL DEFAULT 'POSTED',
    external_ref    VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ipd_charge_day UNIQUE (tenant_id, shop_id, admission_id, charge_date, charge_type)
);

CREATE INDEX IF NOT EXISTS idx_ipd_charge_admission
    ON ipd_charge_line (tenant_id, shop_id, admission_id, charge_date);
CREATE INDEX IF NOT EXISTS idx_ipd_charge_date
    ON ipd_charge_line (tenant_id, shop_id, charge_date, status);
