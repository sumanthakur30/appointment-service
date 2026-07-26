-- Wave A3: link MAR orders/administrations to pharmacy stock (FEFO).
ALTER TABLE mar_order
    ADD COLUMN IF NOT EXISTS product_id BIGINT,
    ADD COLUMN IF NOT EXISTS dispense_quantity INTEGER;

ALTER TABLE mar_administration
    ADD COLUMN IF NOT EXISTS stock_reservation_key VARCHAR(120),
    ADD COLUMN IF NOT EXISTS stock_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS stock_detail TEXT;
