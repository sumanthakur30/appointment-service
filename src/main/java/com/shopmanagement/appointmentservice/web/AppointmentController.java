package com.shopmanagement.appointmentservice.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.appointmentservice.model.Appointment;
import com.shopmanagement.appointmentservice.model.AppointmentDoctorChange;
import com.shopmanagement.appointmentservice.service.AppointmentService;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> list(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.listToday(doctorId, date);
    }

    @GetMapping("/today")
    public List<Appointment> today(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.listToday(doctorId, date);
    }

    @GetMapping("/patient/{patientId}")
    public List<Appointment> patientHistory(
            @PathVariable Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer limit) {
        return appointmentService.listPatientHistory(patientId, from, to, limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment book(@RequestBody Appointment appointment) {
        return appointmentService.book(appointment);
    }

    @PostMapping("/walk-in")
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment walkIn(@RequestBody Appointment appointment) {
        return appointmentService.walkIn(appointment);
    }

    @PutMapping("/{id}/check-in")
    public Appointment checkIn(@PathVariable Long id) {
        return appointmentService.checkIn(id);
    }

    @PutMapping("/{id}/cancel")
    public Appointment cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return appointmentService.cancel(id, reason);
    }

    @PutMapping("/{id}")
    public Appointment update(@PathVariable Long id, @RequestBody Appointment appointment) {
        return appointmentService.update(id, appointment);
    }

    @GetMapping("/{id}/doctor-changes")
    public List<AppointmentDoctorChange> doctorChanges(@PathVariable Long id) {
        return appointmentService.listDoctorChanges(id);
    }

    @PutMapping("/{id}/complete")
    public Appointment complete(@PathVariable Long id) {
        return appointmentService.complete(id);
    }
}
