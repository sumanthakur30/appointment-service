-- Phase 3: OT booking, infection control, visitor / family portal

CREATE TABLE IF NOT EXISTS ot_booking (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    booking_no      VARCHAR(64) NOT NULL,
    theatre_code    VARCHAR(64),
    theatre_name    VARCHAR(191),
    procedure_name  VARCHAR(255) NOT NULL,
    surgeon_name    VARCHAR(191),
    anaesthetist    VARCHAR(191),
    scheduled_start TIMESTAMP NOT NULL,
    scheduled_end   TIMESTAMP,
    status          VARCHAR(32) NOT NULL DEFAULT 'REQUESTED',
    preop_notes     TEXT,
    postop_notes    TEXT,
    recovery_bed_id BIGINT,
    created_by      VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ot_booking_no UNIQUE (tenant_id, shop_id, booking_no)
);

CREATE INDEX IF NOT EXISTS idx_ot_booking_admission
    ON ot_booking (tenant_id, shop_id, admission_id);
CREATE INDEX IF NOT EXISTS idx_ot_booking_status
    ON ot_booking (tenant_id, shop_id, status, scheduled_start);

CREATE TABLE IF NOT EXISTS infection_isolation (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    isolation_type  VARCHAR(64) NOT NULL,
    pathogen        VARCHAR(128),
    ppe_required    VARCHAR(255),
    cleaning_notes  TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    started_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at        TIMESTAMP,
    created_by      VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_infection_admission
    ON infection_isolation (tenant_id, shop_id, admission_id, active);

CREATE TABLE IF NOT EXISTS visitor_pass (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    pass_code       VARCHAR(32) NOT NULL,
    visitor_name    VARCHAR(191) NOT NULL,
    relation        VARCHAR(64),
    phone           VARCHAR(32),
    visiting_hours  VARCHAR(128),
    valid_from      TIMESTAMP NOT NULL DEFAULT NOW(),
    valid_to        TIMESTAMP,
    status          VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(128),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_visitor_pass_code UNIQUE (tenant_id, shop_id, pass_code)
);

CREATE INDEX IF NOT EXISTS idx_visitor_pass_admission
    ON visitor_pass (tenant_id, shop_id, admission_id, status);
CREATE INDEX IF NOT EXISTS idx_visitor_pass_code
    ON visitor_pass (tenant_id, shop_id, pass_code);
