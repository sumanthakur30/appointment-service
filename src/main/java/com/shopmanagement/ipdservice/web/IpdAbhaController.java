package com.shopmanagement.ipdservice.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.abha.IpdAbhaLink;
import com.shopmanagement.ipdservice.abha.IpdAbhaService;

@RestController
@RequestMapping("/ipd")
public class IpdAbhaController {

    private final IpdAbhaService abhaService;

    public IpdAbhaController(IpdAbhaService abhaService) {
        this.abhaService = abhaService;
    }

    @GetMapping("/abha/ndhm-status")
    public Map<String, Object> ndhmStatus() {
        return abhaService.ndhmStatus();
    }

    @GetMapping("/patients/{patientId}/abha")
    public IpdAbhaLink get(@PathVariable Long patientId) {
        return abhaService.findByPatient(patientId);
    }

    @PutMapping("/patients/{patientId}/abha")
    public IpdAbhaLink link(@PathVariable Long patientId, @RequestBody IpdAbhaLink body) {
        return abhaService.link(patientId, body);
    }

    @PostMapping("/patients/{patientId}/abha/otp")
    public Map<String, Object> requestOtp(@PathVariable Long patientId) {
        return abhaService.requestOtp(patientId);
    }

    @PostMapping("/patients/{patientId}/abha/consent")
    public IpdAbhaLink confirmConsent(@PathVariable Long patientId, @RequestBody ConsentRequest body) {
        return abhaService.confirmConsent(patientId, body == null ? null : body.txnId(), body == null ? null : body.otp());
    }

    public record ConsentRequest(String txnId, String otp) {}
}
