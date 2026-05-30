ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS last_edited_by VARCHAR(120);

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS last_edited_at TIMESTAMP;
