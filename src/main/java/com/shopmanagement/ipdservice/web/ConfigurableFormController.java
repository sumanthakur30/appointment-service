package com.shopmanagement.ipdservice.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.forms.ConfigurableFormService;
import com.shopmanagement.ipdservice.forms.InsurerClaimPublishService;
import com.shopmanagement.ipdservice.forms.IpdFormSubmission;

@RestController
@RequestMapping("/ipd/forms")
public class ConfigurableFormController {

    private final ConfigurableFormService configurableFormService;
    private final InsurerClaimPublishService insurerClaimPublishService;

    public ConfigurableFormController(
            ConfigurableFormService configurableFormService,
            InsurerClaimPublishService insurerClaimPublishService) {
        this.configurableFormService = configurableFormService;
        this.insurerClaimPublishService = insurerClaimPublishService;
    }

    @GetMapping("/bootstrap")
    public Map<String, Object> bootstrap(
            @RequestParam String purpose,
            @RequestParam(required = false) String claimFormat) {
        return configurableFormService.bootstrap(purpose, claimFormat);
    }

    @PostMapping("/tpa-claims/publish")
    public Map<String, Object> publishTpaClaims() {
        return insurerClaimPublishService.publishPack();
    }

    @GetMapping("/admissions/{admissionId}")
    public List<IpdFormSubmission> list(@PathVariable Long admissionId) {
        return configurableFormService.list(admissionId);
    }

    @PostMapping("/admissions/{admissionId}")
    public IpdFormSubmission submit(
            @PathVariable Long admissionId,
            @RequestBody SubmitRequest body) {
        return configurableFormService.submit(admissionId, body.purpose(), body.answers());
    }

    public record SubmitRequest(String purpose, Map<String, Object> answers) {}
}
