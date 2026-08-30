package com.shopmanagement.appointmentservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.shopmanagement.appointmentservice.client.EncounterReassignClient;
import com.shopmanagement.appointmentservice.client.QueueReassignClient;
import com.shopmanagement.appointmentservice.model.Appointment;
import com.shopmanagement.appointmentservice.model.AppointmentDoctorChange;
import com.shopmanagement.appointmentservice.repository.AppointmentDoctorChangeRepository;
import com.shopmanagement.appointmentservice.repository.AppointmentRepository;
import com.shopmanagement.appointmentservice.support.TenantContext;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentDoctorChangeRepository doctorChangeRepository;
    private final QueueReassignClient queueReassignClient;
    private final EncounterReassignClient encounterReassignClient;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AppointmentDoctorChangeRepository doctorChangeRepository,
            QueueReassignClient queueReassignClient,
            EncounterReassignClient encounterReassignClient) {
        this.appointmentRepository = appointmentRepository;
        this.doctorChangeRepository = doctorChangeRepository;
        this.queueReassignClient = queueReassignClient;
        this.encounterReassignClient = encounterReassignClient;
    }

    @Transactional(readOnly = true)
    public List<Appointment> listToday(Long doctorId, LocalDate date) {
        TenantContext.requireAnyPermission(
                "MANAGE_APPOINTMENTS", "MANAGE_QUEUE", "MANAGE_CONSULTATIONS", "VIEW_DOCTOR_DASHBOARD");
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

    @Transactional(readOnly = true)
    public List<Appointment> listPatientHistory(Long patientId, LocalDate from, LocalDate to, Integer limit) {
        TenantContext.requireAnyPermission(
                "MANAGE_APPOINTMENTS",
                "MANAGE_CONSULTATIONS",
                "VIEW_PATIENT_HISTORY",
                "MANAGE_CUSTOMERS");
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("patientId is required");
        }
        Long tenantId = TenantContext.requireTenantId();
        LocalDate toDate = to != null ? to : LocalDate.now();
        LocalDate fromDate = from != null ? from : toDate.minusDays(365);
        int max = limit != null && limit > 0 ? Math.min(limit, 300) : 100;
        return appointmentRepository
                .findByTenantIdAndPatientIdAndAppointmentDateGreaterThanEqualAndAppointmentDateLessThanEqualOrderByAppointmentDateDescStartTimeDesc(
                        tenantId,
                        patientId,
                        fromDate,
                        toDate)
                .stream()
                .limit(max)
                .toList();
    }

    @Transactional
    public Appointment book(Appointment appointment) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_CONSULTATIONS");
        return saveNew(appointment, "SCHEDULED", "BOOKED", "RECEPTION");
    }

    @Transactional
    public Appointment walkIn(Appointment appointment) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_QUEUE");
        String visitType = appointment.getType();
        if (visitType == null || visitType.isBlank()) {
            visitType = "WALK_IN";
        }
        Appointment saved = saveNew(appointment, visitType.trim().toUpperCase(), "CONFIRMED", "RECEPTION");
        saved.setStatus("CHECKED_IN");
        return appointmentRepository.save(saved);
    }

    @Transactional
    public Appointment checkIn(Long id) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_QUEUE");
        Appointment appointment = require(id);
        appointment.setStatus("CHECKED_IN");
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancel(Long id, String reason) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_QUEUE");
        Appointment appointment = require(id);
        appointment.setStatus("CANCELLED");
        appointment.setCancellationReason(reason);
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDoctorChange> listDoctorChanges(Long appointmentId) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_QUEUE");
        require(appointmentId);
        return doctorChangeRepository.findByTenantIdAndShopIdAndAppointmentIdOrderByChangedAtAsc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), appointmentId);
    }

    @Transactional
    public Appointment update(Long id, Appointment payload) {
        TenantContext.requireAnyPermission("MANAGE_APPOINTMENTS", "MANAGE_QUEUE");
        Appointment appointment = require(id);
        if ("COMPLETED".equalsIgnoreCase(appointment.getStatus()) || "CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException("Completed or cancelled appointments cannot be edited");
        }

        Long previousDoctorId = appointment.getDoctorId();
        Double previousFee = appointment.getConsultationFee();
        boolean doctorChanged = payload.getDoctorId() != null && !payload.getDoctorId().equals(previousDoctorId);

        if (payload.getBranchId() != null) {
            appointment.setBranchId(payload.getBranchId());
        }
        if (payload.getDoctorId() != null) {
            appointment.setDoctorId(payload.getDoctorId());
        }
        if (payload.getConsultationFee() != null) {
            appointment.setConsultationFee(payload.getConsultationFee());
        }
        if (payload.getChiefComplaint() != null) {
            appointment.setChiefComplaint(payload.getChiefComplaint());
        }
        if (payload.getPaymentType() != null) {
            appointment.setPaymentType(payload.getPaymentType());
        }
        if (payload.getType() != null && !payload.getType().isBlank()) {
            appointment.setType(payload.getType().trim().toUpperCase());
        }
        if (payload.getDepartmentId() != null) {
            appointment.setDepartmentId(payload.getDepartmentId());
        }
        if (payload.getStartTime() != null) {
            appointment.setStartTime(payload.getStartTime());
        }
        if (payload.getEndTime() != null) {
            appointment.setEndTime(payload.getEndTime());
        }
        appointment.setLastEditedBy(TenantContext.currentActor());
        appointment.setLastEditedAt(LocalDateTime.now());

        validateRequired(appointment);
        Appointment saved = appointmentRepository.save(appointment);

        if (doctorChanged) {
            AppointmentDoctorChange change = new AppointmentDoctorChange();
            change.setTenantId(TenantContext.requireTenantId());
            change.setShopId(TenantContext.requireShopId());
            change.setAppointmentId(saved.getId());
            change.setPreviousDoctorId(previousDoctorId);
            change.setNewDoctorId(saved.getDoctorId());
            change.setPreviousConsultationFee(previousFee);
            change.setNewConsultationFee(saved.getConsultationFee());
            change.setChangedBy(TenantContext.currentActor());
            change.setChangedAt(LocalDateTime.now());
            doctorChangeRepository.save(change);
        }

        if (payload.getDoctorId() != null) {
            Long appointmentId = saved.getId();
            Long doctorId = saved.getDoctorId();
            String doctorName = payload.getDoctorName();
            Double consultationFee = saved.getConsultationFee();
            Double followupFee = payload.getFollowupFee();
            String visitType = saved.getType();
            Runnable moveLiveVisit = () -> {
                queueReassignClient.reassignAppointmentDoctor(appointmentId, doctorId);
                encounterReassignClient.reassignVisitDoctor(
                        appointmentId,
                        doctorId,
                        doctorName,
                        consultationFee,
                        followupFee,
                        visitType);
            };
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        moveLiveVisit.run();
                    }
                });
            } else {
                moveLiveVisit.run();
            }
        }
        return saved;
    }

    @Transactional
    public Appointment complete(Long id) {
        TenantContext.requireAnyPermission("MANAGE_CONSULTATIONS", "WRITE_PRESCRIPTION", "VIEW_DOCTOR_DASHBOARD");
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
