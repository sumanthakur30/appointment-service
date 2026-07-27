-- Wave P0: waitlist timestamps on admission
ALTER TABLE ipd_admission
    ADD COLUMN IF NOT EXISTS waitlisted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS expected_admit_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS waitlist_rank INT;

ALTER TABLE mar_administration
    ADD COLUMN IF NOT EXISTS allergy_override BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS allergy_match TEXT;
