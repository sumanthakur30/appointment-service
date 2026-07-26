-- Phase 4: configurable form submissions (assessment / consent) + consent snapshot on admission

CREATE TABLE IF NOT EXISTS ipd_form_submission (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    shop_id         VARCHAR(100) NOT NULL,
    admission_id    BIGINT NOT NULL REFERENCES ipd_admission(id),
    form_key        VARCHAR(128) NOT NULL,
    form_title      VARCHAR(255),
    purpose         VARCHAR(64) NOT NULL,
    answers_json    TEXT NOT NULL DEFAULT '{}',
    schema_json     TEXT,
    status          VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    submitted_by    VARCHAR(128),
    submitted_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ipd_form_submission_admission
    ON ipd_form_submission (tenant_id, shop_id, admission_id, purpose);

ALTER TABLE ipd_admission
    ADD COLUMN IF NOT EXISTS consent_form_key VARCHAR(128);

ALTER TABLE ipd_admission
    ADD COLUMN IF NOT EXISTS consent_answers_json TEXT;

ALTER TABLE ipd_admission
    ADD COLUMN IF NOT EXISTS consent_captured_at TIMESTAMP;
