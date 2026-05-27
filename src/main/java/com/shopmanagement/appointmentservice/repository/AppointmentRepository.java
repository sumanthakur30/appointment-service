package com.shopmanagement.appointmentservice.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.appointmentservice.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByTenantIdAndShopIdAndAppointmentDateOrderByStartTimeAsc(
            Long tenantId, String shopId, LocalDate appointmentDate);

    List<Appointment> findByTenantIdAndShopIdAndDoctorIdAndAppointmentDateOrderByStartTimeAsc(
            Long tenantId, String shopId, Long doctorId, LocalDate appointmentDate);

    Optional<Appointment> findByIdAndTenantIdAndShopId(Long id, Long tenantId, String shopId);
}
