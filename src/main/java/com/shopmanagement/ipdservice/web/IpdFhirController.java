package com.shopmanagement.ipdservice.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.fhir.IpdFhirExport;
import com.shopmanagement.ipdservice.fhir.IpdFhirExportService;

@RestController
@RequestMapping("/ipd")
public class IpdFhirController {

    private final IpdFhirExportService fhirExportService;

    public IpdFhirController(IpdFhirExportService fhirExportService) {
        this.fhirExportService = fhirExportService;
    }

    @GetMapping("/admissions/{admissionId}/fhir/bundle")
    public Map<String, Object> preview(@PathVariable Long admissionId) {
        return fhirExportService.previewBundle(admissionId);
    }

    @PostMapping("/admissions/{admissionId}/fhir/export")
    public IpdFhirExport export(@PathVariable Long admissionId) {
        return fhirExportService.exportAndPersist(admissionId);
    }

    @GetMapping("/admissions/{admissionId}/fhir/exports")
    public List<IpdFhirExport> forAdmission(@PathVariable Long admissionId) {
        return fhirExportService.listForAdmission(admissionId);
    }

    @GetMapping("/fhir/exports")
    public List<IpdFhirExport> list() {
        return fhirExportService.listExports();
    }
}
