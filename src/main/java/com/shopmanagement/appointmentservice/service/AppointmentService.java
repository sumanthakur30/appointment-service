package com.shopmanagement.appointmentservice.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.appointmentservice.model.Appointment;
import com.shopmanagement.appointmentservice.repository.AppointmentRepository;
import com.shopmanagement.appointmentservice.support.TenantContext;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> listToday(Long doctorId, LocalDate date) {
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        LocalDate targetDate = date != null ? date : LocalDate.now();
        if (doctorId != null) {
            return appointmentRepository.findByTenantIdAndShopIdAndDoctorIdAndAppointmentDateOrderByStartTimeAsc(
                    tenantId, shopId, doctorId, targetDate);
        }
        return appointmentRepository.findByTenantIdAndShopIdAndAppointmentDateOrderByStartTimeAsc(
                tenantId, shopId, targetDate);
    }

    @Transactional
    public Appointment book(Appointment appointment) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_CONSULTATIONS", "MANAGE_ORDERS");
        return saveNew(appointment, "SCHEDULED", "BOOKED", "RECEPTION");
    }

    @Transactional
    public Appointment walkIn(Appointment appointment) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_CONSULTATIONS", "MANAGE_ORDERS");
        Appointment saved = saveNew(appointment, "WALK_IN", "CONFIRMED", "RECEPTION");
        saved.setStatus("CHECKED_IN");
        return appointmentRepository.save(saved);
    }

    @Transactional
    public Appointment checkIn(Long id) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_CONSULTATIONS", "MANAGE_ORDERS");
        Appointment appointment = require(id);
        appointment.setStatus("CHECKED_IN");
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancel(Long id, String reason) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_CONSULTATIONS", "MANAGE_ORDERS");
        Appointment appointment = require(id);
        appointment.setStatus("CANCELLED");
        appointment.setCancellationReason(reason);
        return appointmentRepository.save(appointment);
    }

  @Transactional
    public Appointment complete(Long id) {
        Appointment appointment = require(id);
        appointment.setStatus("COMPLETED");
        return appointmentRepository.save(appointment);
    }

    Appointment require(Long id) {
        return appointmentRepository.findByIdAndTenantIdAndShopId(
                        id, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + id));
    }

    private Appointment saveNew(Appointment appointment, String type, String status, String bookedVia) {
        validateRequired(appointment);
        appointment.setId(null);
        appointment.setTenantId(TenantContext.requireTenantId());
        appointment.setShopId(TenantContext.requireShopId());
        appointment.setType(type);
        appointment.setStatus(status);
        appointment.setBookedVia(bookedVia);
        if (appointment.getAppointmentDate() == null) {
            appointment.setAppointmentDate(LocalDate.now());
        }
        if (appointment.getStartTime() == null) {
            appointment.setStartTime(LocalTime.now().withSecond(0).withNano(0));
        }
        if (appointment.getEndTime() == null) {
            appointment.setEndTime(appointment.getStartTime().plusMinutes(15));
        }
        return appointmentRepository.save(appointment);
    }

    private static void validateRequired(Appointment appointment) {
        if (appointment.getBranchId() == null) {
            throw new IllegalArgumentException("branchId is required");
        }
        if (appointment.getPatientId() == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        if (appointment.getDoctorId() == null) {
            throw new IllegalArgumentException("doctorId is required");
        }
    }
}
