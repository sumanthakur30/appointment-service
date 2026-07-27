-- Patient demographics for bed eligibility (gender / age rules).
ALTER TABLE ipd_admission
    ADD COLUMN IF NOT EXISTS patient_gender VARCHAR(16),
    ADD COLUMN IF NOT EXISTS patient_age_years INT;

COMMENT ON COLUMN ipd_admission.patient_gender IS
    'MALE / FEMALE / OTHER — used for gender-restricted bed allocation';
COMMENT ON COLUMN ipd_admission.patient_age_years IS
    'Age in years at admission — used for pediatric/geriatric bed bands';
