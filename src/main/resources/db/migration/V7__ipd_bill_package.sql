-- Wave A6: package amount + final IP bill snapshot
ALTER TABLE ipd_admission
    ADD COLUMN IF NOT EXISTS package_amount NUMERIC(12, 2);

CREATE TABLE IF NOT EXISTS ipd_bill (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    admission_id BIGINT NOT NULL REFERENCES ipd_admission(id),
    admission_no VARCHAR(64) NOT NULL,
    bill_type VARCHAR(16) NOT NULL,
    package_code VARCHAR(64),
    package_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    charge_total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    deposit_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    gross_payable NUMERIC(12, 2) NOT NULL DEFAULT 0,
    amount_due NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'FINALIZED',
    lines_json TEXT,
    finalized_at TIMESTAMP,
    finalized_by VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_ipd_bill_final_admission
    ON ipd_bill (tenant_id, shop_id, admission_id)
    WHERE bill_type = 'FINAL';

CREATE INDEX IF NOT EXISTS idx_ipd_bill_admission
    ON ipd_bill (tenant_id, shop_id, admission_id);
