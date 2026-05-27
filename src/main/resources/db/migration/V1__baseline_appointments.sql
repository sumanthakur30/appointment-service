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
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_appointments_doctor_date
    ON appointments (tenant_id, shop_id, doctor_id, appointment_date, status);

CREATE INDEX IF NOT EXISTS ix_appointments_patient
    ON appointments (tenant_id, shop_id, patient_id, appointment_date DESC);
