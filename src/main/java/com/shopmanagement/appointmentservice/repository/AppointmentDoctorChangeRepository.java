package com.shopmanagement.appointmentservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.appointmentservice.model.AppointmentDoctorChange;

public interface AppointmentDoctorChangeRepository extends JpaRepository<AppointmentDoctorChange, Long> {

    List<AppointmentDoctorChange> findByTenantIdAndShopIdAndAppointmentIdOrderByChangedAtAsc(
            Long tenantId, String shopId, Long appointmentId);
}
