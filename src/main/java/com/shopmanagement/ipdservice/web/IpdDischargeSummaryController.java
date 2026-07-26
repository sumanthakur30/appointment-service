package com.shopmanagement.ipdservice.web;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.forms.ConfigurableFormService;
import com.shopmanagement.ipdservice.forms.IpdFormSubmission;

@RestController
@RequestMapping("/ipd/admissions")
public class IpdDischargeSummaryController {

    private final ConfigurableFormService configurableFormService;

    public IpdDischargeSummaryController(ConfigurableFormService configurableFormService) {
        this.configurableFormService = configurableFormService;
    }

    @GetMapping("/{id}/discharge-summary")
    public ResponseEntity<?> latest(@PathVariable Long id) {
        Optional<IpdFormSubmission> row = configurableFormService.latestDischargeSummary(id);
        if (row.isEmpty()) {
            return ResponseEntity.ok(Map.of("present", false));
        }
        IpdFormSubmission s = row.get();
        return ResponseEntity.ok(Map.of(
                "present", true,
                "id", s.getId(),
                "formKey", s.getFormKey() == null ? "" : s.getFormKey(),
                "formTitle", s.getFormTitle() == null ? "" : s.getFormTitle(),
                "answersJson", s.getAnswersJson() == null ? "{}" : s.getAnswersJson(),
                "submittedAt", s.getSubmittedAt() == null ? "" : s.getSubmittedAt().toString(),
                "submittedBy", s.getSubmittedBy() == null ? "" : s.getSubmittedBy()));
    }

    @GetMapping("/{id}/discharge-summary/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        byte[] bytes = configurableFormService.dischargeSummaryPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"discharge-summary-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
