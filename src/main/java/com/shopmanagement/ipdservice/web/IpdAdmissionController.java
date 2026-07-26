package com.shopmanagement.ipdservice.web;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionChartService;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionService;
import com.shopmanagement.ipdservice.clinical.IpdTransfer;

@RestController
@RequestMapping("/ipd/admissions")
public class IpdAdmissionController {

    private final IpdAdmissionService admissionService;
    private final IpdAdmissionChartService chartService;

    public IpdAdmissionController(IpdAdmissionService admissionService, IpdAdmissionChartService chartService) {
        this.admissionService = admissionService;
        this.chartService = chartService;
    }

    @GetMapping
    public List<IpdAdmission> list() {
        return admissionService.list();
    }

    @GetMapping("/{id}")
    public IpdAdmission get(@PathVariable Long id) {
        return admissionService.get(id);
    }

    @PostMapping
    public IpdAdmission create(@RequestBody IpdAdmission body) {
        return admissionService.create(body);
    }

    @PostMapping("/{id}/allocate-bed")
    public IpdAdmission allocateBed(@PathVariable Long id, @RequestBody AllocateBedRequest body) {
        return admissionService.allocateBed(id, body.bedId(), body.reserveOnly(), body.expectedDischargeAt());
    }

    @PostMapping("/{id}/transfer")
    public IpdTransfer transfer(@PathVariable Long id, @RequestBody TransferRequest body) {
        return admissionService.transfer(id, body.toBedId(), body.reason());
    }

    @PostMapping("/{id}/discharge")
    public IpdAdmission discharge(@PathVariable Long id, @RequestBody(required = false) DischargeRequest body) {
        return admissionService.discharge(id, body == null ? null : body.notes());
    }

    @GetMapping("/{id}/transfers")
    public List<IpdTransfer> transfers(@PathVariable Long id) {
        return admissionService.transfersFor(id);
    }

    @GetMapping("/{id}/chart")
    public java.util.Map<String, Object> chart(@PathVariable Long id) {
        return chartService.chart(id);
    }

    @PostMapping("/{id}/lab-orders")
    public java.util.Map<String, Object> placeLabOrder(
            @PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        return chartService.placeLabOrder(id, body);
    }

    @PostMapping("/{id}/tpa")
    public IpdAdmission updateTpa(@PathVariable Long id, @RequestBody IpdAdmission body) {
        return admissionService.updateTpa(id, body);
    }

    @GetMapping("/{id}/tpa/claim-export")
    public java.util.Map<String, Object> tpaClaimExport(@PathVariable Long id) {
        return admissionService.tpaClaimExport(id);
    }

    public record AllocateBedRequest(Long bedId, boolean reserveOnly, LocalDateTime expectedDischargeAt) {}

    public record TransferRequest(Long toBedId, String reason) {}

    public record DischargeRequest(String notes) {}
}
