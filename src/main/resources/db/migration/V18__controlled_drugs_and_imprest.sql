-- Controlled-drug register + ward floor-stock imprest (config-driven pharmacy P1)
ALTER TABLE mar_order
    ADD COLUMN IF NOT EXISTS controlled_drug BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE mar_administration
    ADD COLUMN IF NOT EXISTS witness_id VARCHAR(128);

CREATE TABLE IF NOT EXISTS controlled_drug_register_entry (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    admission_id BIGINT,
    ward_code VARCHAR(64),
    product_id BIGINT,
    medicine_name VARCHAR(255) NOT NULL,
    batch_no VARCHAR(64),
    txn_type VARCHAR(32) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL,
    unit VARCHAR(32) DEFAULT 'UNIT',
    nurse_id VARCHAR(128),
    witness_id VARCHAR(128),
    mar_administration_id BIGINT,
    balance_after NUMERIC(12, 3),
    notes TEXT,
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_cdr_tenant_shop_time
    ON controlled_drug_register_entry (tenant_id, shop_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_cdr_product
    ON controlled_drug_register_entry (tenant_id, shop_id, product_id, medicine_name);

CREATE TABLE IF NOT EXISTS ward_imprest_location (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    ward_code VARCHAR(64) NOT NULL,
    ward_name VARCHAR(191),
    warehouse_ref VARCHAR(128),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ward_imprest UNIQUE (tenant_id, shop_id, ward_code)
);

CREATE TABLE IF NOT EXISTS imprest_par_level (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    imprest_location_id BIGINT NOT NULL REFERENCES ward_imprest_location(id),
    product_id BIGINT,
    medicine_name VARCHAR(255) NOT NULL,
    min_qty NUMERIC(12, 3) NOT NULL DEFAULT 0,
    par_qty NUMERIC(12, 3) NOT NULL DEFAULT 0,
    max_qty NUMERIC(12, 3),
    on_hand_qty NUMERIC(12, 3) NOT NULL DEFAULT 0,
    unit VARCHAR(32) DEFAULT 'UNIT',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_imprest_par_loc
    ON imprest_par_level (tenant_id, shop_id, imprest_location_id);

CREATE TABLE IF NOT EXISTS imprest_txn (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(64) NOT NULL,
    imprest_location_id BIGINT NOT NULL REFERENCES ward_imprest_location(id),
    product_id BIGINT,
    medicine_name VARCHAR(255) NOT NULL,
    txn_type VARCHAR(32) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL,
    unit VARCHAR(32) DEFAULT 'UNIT',
    reference_no VARCHAR(128),
    notes TEXT,
    recorded_by VARCHAR(128),
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_imprest_txn_loc
    ON imprest_txn (tenant_id, shop_id, imprest_location_id, recorded_at DESC);
