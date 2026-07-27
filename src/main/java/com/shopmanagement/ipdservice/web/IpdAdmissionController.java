package com.shopmanagement.ipdservice.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionChartService;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionService;
import com.shopmanagement.ipdservice.clinical.IpdTransfer;
import com.shopmanagement.ipdservice.clinical.TpaClaimPdfRenderer;
import com.shopmanagement.ipdservice.forms.ConfigurableFormService;

@RestController
@RequestMapping("/ipd/admissions")
public class IpdAdmissionController {

    private final IpdAdmissionService admissionService;
    private final IpdAdmissionChartService chartService;
    private final TpaClaimPdfRenderer tpaClaimPdfRenderer;
    private final ConfigurableFormService configurableFormService;

    public IpdAdmissionController(
            IpdAdmissionService admissionService,
            IpdAdmissionChartService chartService,
            TpaClaimPdfRenderer tpaClaimPdfRenderer,
            ConfigurableFormService configurableFormService) {
        this.admissionService = admissionService;
        this.chartService = chartService;
        this.tpaClaimPdfRenderer = tpaClaimPdfRenderer;
        this.configurableFormService = configurableFormService;
    }

    @GetMapping
    public List<IpdAdmission> list() {
        return admissionService.list();
    }

    @GetMapping("/waitlist")
    public List<IpdAdmission> waitlist() {
        return admissionService.waitlist();
    }

    @GetMapping("/{id}")
    public IpdAdmission get(@PathVariable Long id) {
        return admissionService.get(id);
    }

    @PostMapping
    public IpdAdmission create(@RequestBody IpdAdmission body) {
        return admissionService.create(body);
    }

    @PostMapping("/{id}/waitlist")
    public IpdAdmission placeOnWaitlist(@PathVariable Long id, @RequestBody(required = false) WaitlistRequest body) {
        return admissionService.placeOnWaitlist(
                id,
                body == null ? null : body.rank(),
                body == null ? null : body.expectedAdmitAt());
    }

    @PostMapping("/{id}/waitlist/promote")
    public IpdAdmission promoteFromWaitlist(@PathVariable Long id) {
        return admissionService.promoteFromWaitlist(id);
    }

    @PostMapping("/{id}/allocate-bed")
    public IpdAdmission allocateBed(@PathVariable Long id, @RequestBody AllocateBedRequest body) {
        return admissionService.allocateBed(id, body.bedId(), body.reserveOnly(), body.expectedDischargeAt());
    }

    @GetMapping("/{id}/eligible-beds")
    public List<com.shopmanagement.ipdservice.accommodation.client.AccommodationDtos.AccommodationBedDto> eligibleBeds(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean availableOnly) {
        return admissionService.eligibleBeds(id, availableOnly);
    }

    @PostMapping("/{id}/transfer")
    public IpdTransfer transfer(@PathVariable Long id, @RequestBody TransferRequest body) {
        return admissionService.transfer(id, body.toBedId(), body.reason(), body.checklistSubmissionId());
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
    public Map<String, Object> chart(@PathVariable Long id) {
        return chartService.chart(id);
    }

    @PostMapping("/{id}/lab-orders")
    public Map<String, Object> placeLabOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return chartService.placeLabOrder(id, body);
    }

    @PostMapping("/{id}/tpa")
    public IpdAdmission updateTpa(@PathVariable Long id, @RequestBody IpdAdmission body) {
        return admissionService.updateTpa(id, body);
    }

    @GetMapping("/{id}/tpa/claim-export")
    public Map<String, Object> tpaClaimExport(@PathVariable Long id) {
        return admissionService.tpaClaimExport(id);
    }

    @GetMapping("/{id}/tpa/claim-form")
    public Map<String, Object> tpaClaimForm(
            @PathVariable Long id,
            @RequestParam(required = false) String format) {
        IpdAdmission admission = admissionService.get(id);
        String claimFormat = format != null && !format.isBlank()
                ? format.trim().toUpperCase()
                : admissionService.resolveClaimFormat(admission);
        Map<String, Object> bootstrap =
                configurableFormService.bootstrap(ConfigurableFormService.PURPOSE_TPA_CLAIM, claimFormat);
        bootstrap.put("answers", admissionService.tpaClaimExport(id));
        bootstrap.put("claimFormat", claimFormat);
        return bootstrap;
    }

    @GetMapping("/{id}/tpa/claim-export/pdf")
    public ResponseEntity<byte[]> tpaClaimPdf(
            @PathVariable Long id,
            @RequestParam(required = false) String format) {
        IpdAdmission admission = admissionService.get(id);
        String claimFormat = format != null && !format.isBlank()
                ? format.trim().toUpperCase()
                : admissionService.resolveClaimFormat(admission);
        Map<String, Object> bootstrap =
                configurableFormService.bootstrap(ConfigurableFormService.PURPOSE_TPA_CLAIM, claimFormat);
        @SuppressWarnings("unchecked")
        Map<String, Object> form = (Map<String, Object>) bootstrap.get("form");
        Map<String, Object> answers = admissionService.tpaClaimExport(id);
        answers.put("claimFormat", claimFormat);
        byte[] pdf = tpaClaimPdfRenderer.render(admission, form, answers);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"tpa-claim-" + claimFormat + "-" + admission.getAdmissionNo() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    public record AllocateBedRequest(Long bedId, boolean reserveOnly, LocalDateTime expectedDischargeAt) {}

    public record TransferRequest(Long toBedId, String reason, Long checklistSubmissionId) {}

    public record DischargeRequest(String notes) {}

    public record WaitlistRequest(Integer rank, LocalDateTime expectedAdmitAt) {}
}
