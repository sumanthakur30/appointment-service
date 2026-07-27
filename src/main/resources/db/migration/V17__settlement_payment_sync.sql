-- Settlement → order-service payment sync columns
ALTER TABLE ipd_settlement_entry
    ADD COLUMN IF NOT EXISTS sync_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS external_payment_id BIGINT,
    ADD COLUMN IF NOT EXISTS external_order_ids TEXT,
    ADD COLUMN IF NOT EXISTS sync_error TEXT,
    ADD COLUMN IF NOT EXISTS synced_at TIMESTAMP;

COMMENT ON COLUMN ipd_settlement_entry.sync_status IS
    'PENDING | SYNCED | FAILED | SKIPPED — order-service payment sync';
