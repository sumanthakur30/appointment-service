-- Wave P1c: IPD settlement ledger (patient / TPA allocations against final bill)
CREATE TABLE IF NOT EXISTS ipd_settlement_entry (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    admission_id BIGINT NOT NULL,
    bill_id BIGINT,
    entry_type VARCHAR(32) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    direction VARCHAR(8) NOT NULL,
    reference_no VARCHAR(128),
    notes TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'POSTED',
    posted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    posted_by VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ipd_settlement_adm
    ON ipd_settlement_entry (tenant_id, shop_id, admission_id, posted_at DESC);
CREATE INDEX IF NOT EXISTS idx_ipd_settlement_bill
    ON ipd_settlement_entry (tenant_id, shop_id, bill_id);
