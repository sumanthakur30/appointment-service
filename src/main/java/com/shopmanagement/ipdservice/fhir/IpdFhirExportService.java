package com.shopmanagement.ipdservice.fhir;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmanagement.ipdservice.abha.IpdAbhaLink;
import com.shopmanagement.ipdservice.abha.IpdAbhaLinkRepository;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

/**
 * FHIR R4 Bundle export (Patient + Encounter) for an IPD admission.
 * Gated by ipd.fhir.enabled.
 */
@Service
public class IpdFhirExportService {

    private static final DateTimeFormatter FHIR_INSTANT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final IpdAdmissionRepository admissionRepository;
    private final IpdFhirExportRepository exportRepository;
    private final IpdAbhaLinkRepository abhaLinkRepository;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public IpdFhirExportService(
            IpdAdmissionRepository admissionRepository,
            IpdFhirExportRepository exportRepository,
            IpdAbhaLinkRepository abhaLinkRepository,
            ObjectMapper objectMapper,
            @Value("${ipd.fhir.enabled:true}") boolean enabled) {
        this.admissionRepository = admissionRepository;
        this.exportRepository = exportRepository;
        this.abhaLinkRepository = abhaLinkRepository;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    public Map<String, Object> previewBundle(Long admissionId) {
        requireEnabled();
        return buildBundle(requireAdmission(admissionId));
    }

    @Transactional
    public IpdFhirExport exportAndPersist(Long admissionId) {
        requireEnabled();
        IpdAdmission admission = requireAdmission(admissionId);
        Map<String, Object> bundle = buildBundle(admission);
        String json;
        try {
            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize FHIR bundle", ex);
        }
        IpdFhirExport row = new IpdFhirExport();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(admission.getId());
        row.setPatientId(admission.getPatientId());
        row.setResourceType("Bundle");
        row.setFhirJson(json);
        row.setExportedBy(TenantContext.currentActor());
        row.setExportedAt(LocalDateTime.now());
        return exportRepository.save(row);
    }

    public List<IpdFhirExport> listExports() {
        requireEnabled();
        return exportRepository.findByTenantIdAndShopIdOrderByExportedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId());
    }

    public List<IpdFhirExport> listForAdmission(Long admissionId) {
        requireEnabled();
        requireAdmission(admissionId);
        return exportRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByExportedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    private Map<String, Object> buildBundle(IpdAdmission a) {
        String patientId = "Patient/" + a.getPatientId();
        String encounterId = "Encounter/ipd-" + a.getId();
        List<Map<String, Object>> entries = new ArrayList<>();
        entries.add(entry(patientResource(a)));
        entries.add(entry(encounterResource(a, patientId)));

        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("resourceType", "Bundle");
        bundle.put("id", "ipd-adt-" + a.getId() + "-" + UUID.randomUUID().toString().substring(0, 8));
        bundle.put("type", "collection");
        bundle.put("timestamp", FHIR_INSTANT.format(LocalDateTime.now()));
        bundle.put("entry", entries);
        bundle.put("meta", Map.of(
                "tag", List.of(Map.of(
                        "system", "https://sugamflow.local/fhir/tags",
                        "code", "IPD_ADT",
                        "display", "IPD Patient/Encounter export"))));
        return bundle;
    }

    private Map<String, Object> patientResource(IpdAdmission a) {
        Map<String, Object> patient = new LinkedHashMap<>();
        patient.put("resourceType", "Patient");
        patient.put("id", String.valueOf(a.getPatientId()));
        patient.put("active", true);
        if (a.getPatientName() != null && !a.getPatientName().isBlank()) {
            patient.put("name", List.of(Map.of(
                    "use", "official",
                    "text", a.getPatientName().trim())));
        }
        List<Map<String, Object>> identifiers = new ArrayList<>();
        identifiers.add(Map.of(
                "system", "https://sugamflow.local/fhir/patient-id",
                "value", String.valueOf(a.getPatientId())));
        abhaLinkRepository
                .findByTenantIdAndShopIdAndPatientIdAndActiveTrue(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), a.getPatientId())
                .ifPresent(link -> identifiers.add(abhaIdentifier(link)));
        patient.put("identifier", identifiers);
        return patient;
    }

    private static Map<String, Object> abhaIdentifier(IpdAbhaLink link) {
        Map<String, Object> id = new LinkedHashMap<>();
        id.put("system", "https://healthid.ndhm.gov.in");
        id.put("value", link.getAbhaNumber());
        if (link.getAbhaAddress() != null && !link.getAbhaAddress().isBlank()) {
            id.put("type", Map.of("text", "ABHA Address: " + link.getAbhaAddress()));
        }
        return id;
    }

    private Map<String, Object> encounterResource(IpdAdmission a, String patientRef) {
        Map<String, Object> enc = new LinkedHashMap<>();
        enc.put("resourceType", "Encounter");
        enc.put("id", "ipd-" + a.getId());
        enc.put("status", mapEncounterStatus(a.getStatus()));
        enc.put("class", Map.of(
                "system", "http://terminology.hl7.org/CodeSystem/v3-ActCode",
                "code", "IMP",
                "display", "inpatient encounter"));
        enc.put("subject", Map.of("reference", patientRef));
        if (a.getAdmissionNo() != null) {
            enc.put("identifier", List.of(Map.of(
                    "system", "https://sugamflow.local/fhir/admission-no",
                    "value", a.getAdmissionNo())));
        }
        Map<String, Object> period = new LinkedHashMap<>();
        if (a.getAdmittedAt() != null) {
            period.put("start", FHIR_INSTANT.format(a.getAdmittedAt()));
        }
        if (a.getDischargedAt() != null) {
            period.put("end", FHIR_INSTANT.format(a.getDischargedAt()));
        }
        if (!period.isEmpty()) {
            enc.put("period", period);
        }
        if (a.getDepartment() != null && !a.getDepartment().isBlank()) {
            enc.put("serviceType", Map.of("text", a.getDepartment()));
        }
        if (a.getDiagnosis() != null && !a.getDiagnosis().isBlank()) {
            enc.put("reasonCode", List.of(Map.of("text", a.getDiagnosis())));
        }
        if (a.getEncounterId() != null) {
            enc.put("partOf", Map.of("reference", "Encounter/" + a.getEncounterId()));
        }
        return enc;
    }

    private static String mapEncounterStatus(String status) {
        if (status == null) {
            return "unknown";
        }
        return switch (status.toUpperCase()) {
            case "REQUESTED" -> "planned";
            case "ADMITTED", "TRANSFERRED" -> "in-progress";
            case "DISCHARGED" -> "finished";
            case "CANCELLED" -> "cancelled";
            default -> "unknown";
        };
    }

    private static Map<String, Object> entry(Map<String, Object> resource) {
        return Map.of(
                "fullUrl", resource.get("resourceType") + "/" + resource.get("id"),
                "resource", resource);
    }

    private IpdAdmission requireAdmission(Long admissionId) {
        return admissionRepository
                .findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new IllegalStateException("IPD FHIR export is disabled (ipd.fhir.enabled=false)");
        }
    }
}
