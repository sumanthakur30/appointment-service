package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.icu.DeviceObservation;
import com.shopmanagement.ipdservice.icu.DeviceObservationService;

@RestController
@RequestMapping("/ipd/icu")
public class IcuDeviceController {

    private final DeviceObservationService service;

    public IcuDeviceController(DeviceObservationService service) {
        this.service = service;
    }

    @GetMapping("/admissions/{admissionId}/devices")
    public List<DeviceObservation> list(@PathVariable Long admissionId) {
        return service.list(admissionId);
    }

    @PostMapping("/admissions/{admissionId}/devices")
    public DeviceObservation record(@PathVariable Long admissionId, @RequestBody DeviceObservation body) {
        return service.record(admissionId, body);
    }
}
