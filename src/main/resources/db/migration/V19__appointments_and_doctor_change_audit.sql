CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    branch_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    department_id BIGINT,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    chief_complaint VARCHAR(1000),
    consultation_fee DOUBLE PRECISION,
    booked_by_account_id BIGINT,
    booked_via VARCHAR(30),
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_edited_by VARCHAR(120),
    last_edited_at TIMESTAMP,
    payment_type VARCHAR(30)
);

CREATE INDEX IF NOT EXISTS ix_appointments_doctor_date
    ON appointments (tenant_id, shop_id, doctor_id, appointment_date, status);

CREATE INDEX IF NOT EXISTS ix_appointments_patient
    ON appointments (tenant_id, shop_id, patient_id, appointment_date DESC);

CREATE TABLE IF NOT EXISTS appointment_doctor_changes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    shop_id VARCHAR(100) NOT NULL,
    appointment_id BIGINT NOT NULL,
    previous_doctor_id BIGINT NOT NULL,
    new_doctor_id BIGINT NOT NULL,
    previous_consultation_fee DOUBLE PRECISION,
    new_consultation_fee DOUBLE PRECISION,
    changed_by VARCHAR(120) NOT NULL,
    changed_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_appointment_doctor_changes_appt
    ON appointment_doctor_changes (tenant_id, shop_id, appointment_id, changed_at DESC);
