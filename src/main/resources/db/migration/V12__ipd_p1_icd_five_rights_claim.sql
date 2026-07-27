-- Wave P1: ICD coding on discharge + MAR five-rights audit + TPA claim format
ALTER TABLE ipd_admission
    ADD COLUMN IF NOT EXISTS primary_icd_code VARCHAR(32),
    ADD COLUMN IF NOT EXISTS primary_icd_desc VARCHAR(512),
    ADD COLUMN IF NOT EXISTS secondary_icd_codes TEXT,
    ADD COLUMN IF NOT EXISTS tpa_claim_format VARCHAR(64);

ALTER TABLE mar_administration
    ADD COLUMN IF NOT EXISTS five_rights_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS five_rights_detail TEXT,
    ADD COLUMN IF NOT EXISTS patient_id_confirmed VARCHAR(64);
