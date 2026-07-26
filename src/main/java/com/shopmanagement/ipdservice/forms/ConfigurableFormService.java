package com.shopmanagement.ipdservice.forms;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.nursing.NursingNote;
import com.shopmanagement.ipdservice.nursing.NursingNoteRepository;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class ConfigurableFormService {

    public static final String KEY_ASSESSMENT = "ipd_nursing_assessment";
    public static final String KEY_CONSENT = "ipd_admission_consent";
    public static final String KEY_DISCHARGE_SUMMARY = "ipd_discharge_summary";
    public static final String PURPOSE_DISCHARGE_SUMMARY = "DISCHARGE_SUMMARY";
    public static final String KEY_WHO_CHECKLIST = "ipd_who_surgical_checklist";
    public static final String PURPOSE_WHO_CHECKLIST = "WHO_CHECKLIST";

    private final FormDefinitionClient formDefinitionClient;
    private final IpdFormSubmissionRepository submissionRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final NursingNoteRepository nursingNoteRepository;
    private final DischargeSummaryPdfRenderer pdfRenderer;
    private final ObjectMapper objectMapper;
    private final String assessmentFormKey;
    private final String consentFormKey;
    private final String dischargeSummaryFormKey;
    private final String whoChecklistFormKey;

    public ConfigurableFormService(
            FormDefinitionClient formDefinitionClient,
            IpdFormSubmissionRepository submissionRepository,
            IpdAdmissionRepository admissionRepository,
            NursingNoteRepository nursingNoteRepository,
            DischargeSummaryPdfRenderer pdfRenderer,
            ObjectMapper objectMapper,
            @Value("${ipd.forms.assessment-key:ipd_nursing_assessment}") String assessmentFormKey,
            @Value("${ipd.forms.consent-key:ipd_admission_consent}") String consentFormKey,
            @Value("${ipd.forms.discharge-summary-key:ipd_discharge_summary}") String dischargeSummaryFormKey,
            @Value("${ipd.forms.who-checklist-key:ipd_who_surgical_checklist}") String whoChecklistFormKey) {
        this.formDefinitionClient = formDefinitionClient;
        this.submissionRepository = submissionRepository;
        this.admissionRepository = admissionRepository;
        this.nursingNoteRepository = nursingNoteRepository;
        this.pdfRenderer = pdfRenderer;
        this.objectMapper = objectMapper;
        this.assessmentFormKey = assessmentFormKey;
        this.consentFormKey = consentFormKey;
        this.dischargeSummaryFormKey = dischargeSummaryFormKey;
        this.whoChecklistFormKey = whoChecklistFormKey;
    }

    public Map<String, Object> bootstrap(String purpose) {
        String key = resolveKey(purpose);
        Map<String, Object> remote = formDefinitionClient.fetchForm(key);
        Map<String, Object> form = remote;
        if (form == null || form.isEmpty()) {
            form = embeddedFor(purpose, key);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("formKey", form.getOrDefault("formKey", key));
        out.put("purpose", purpose.toUpperCase());
        out.put("form", form);
        out.put("source", remote != null && !remote.isEmpty() ? "FORM_BUILDER" : "EMBEDDED");
        return out;
    }

    public List<IpdFormSubmission> list(Long admissionId) {
        assertAdmission(admissionId);
        return submissionRepository.findByTenantIdAndShopIdAndAdmissionIdOrderBySubmittedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    public Optional<IpdFormSubmission> latestDischargeSummary(Long admissionId) {
        assertAdmission(admissionId);
        List<IpdFormSubmission> rows =
                submissionRepository.findByTenantIdAndShopIdAndAdmissionIdAndPurposeOrderBySubmittedAtDesc(
                        TenantContext.requireTenantId(),
                        TenantContext.requireShopId(),
                        admissionId,
                        PURPOSE_DISCHARGE_SUMMARY);
        return rows.stream().findFirst();
    }

    public byte[] dischargeSummaryPdf(Long admissionId) {
        IpdAdmission admission = assertAdmission(admissionId);
        IpdFormSubmission submission = latestDischargeSummary(admissionId)
                .orElseThrow(() -> new IllegalStateException(
                        "No discharge summary submitted for this admission"));
        return pdfRenderer.render(admission, submission);
    }

    @Transactional
    public IpdFormSubmission submit(Long admissionId, String purpose, Map<String, Object> answers) {
        IpdAdmission admission = assertAdmission(admissionId);
        String key = resolveKey(purpose);
        Map<String, Object> bootstrap = bootstrap(purpose);
        @SuppressWarnings("unchecked")
        Map<String, Object> form = (Map<String, Object>) bootstrap.get("form");
        validateMandatory(form, answers);

        IpdFormSubmission row = new IpdFormSubmission();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setAdmissionId(admissionId);
        row.setFormKey(key);
        row.setFormTitle(String.valueOf(form.getOrDefault("title", key)));
        row.setPurpose(purpose.trim().toUpperCase());
        row.setAnswersJson(toJson(answers));
        row.setSchemaJson(toJson(form));
        row.setStatus("SUBMITTED");
        row.setSubmittedBy(TenantContext.currentActor());
        row.setSubmittedAt(LocalDateTime.now());
        IpdFormSubmission saved = submissionRepository.save(row);

        if ("CONSENT".equalsIgnoreCase(purpose)) {
            admission.setConsentFormKey(key);
            admission.setConsentAnswersJson(row.getAnswersJson());
            admission.setConsentCapturedAt(LocalDateTime.now());
            admissionRepository.save(admission);
        }
        if ("ASSESSMENT".equalsIgnoreCase(purpose)) {
            NursingNote note = new NursingNote();
            note.setTenantId(admission.getTenantId());
            note.setShopId(admission.getShopId());
            note.setAdmissionId(admissionId);
            note.setNoteType("DAILY_ASSESSMENT");
            note.setBody("Configurable nursing assessment submitted (" + key + ")");
            note.setAssessmentJson(row.getAnswersJson());
            note.setRecordedAt(LocalDateTime.now());
            note.setRecordedBy(TenantContext.currentActor());
            nursingNoteRepository.save(note);
        }
        if (PURPOSE_DISCHARGE_SUMMARY.equalsIgnoreCase(purpose)) {
            NursingNote note = new NursingNote();
            note.setTenantId(admission.getTenantId());
            note.setShopId(admission.getShopId());
            note.setAdmissionId(admissionId);
            note.setNoteType("DISCHARGE_SUMMARY");
            note.setBody("Discharge summary submitted (" + key + ")");
            note.setAssessmentJson(row.getAnswersJson());
            note.setRecordedAt(LocalDateTime.now());
            note.setRecordedBy(TenantContext.currentActor());
            nursingNoteRepository.save(note);
        }
        return saved;
    }

    private Map<String, Object> embeddedFor(String purpose, String key) {
        String p = purpose == null ? "" : purpose.trim().toUpperCase();
        if (KEY_CONSENT.equals(key) || "CONSENT".equals(p)) {
            return FormDefinitionClient.embeddedAdmissionConsent();
        }
        if (KEY_DISCHARGE_SUMMARY.equals(key) || PURPOSE_DISCHARGE_SUMMARY.equals(p)) {
            return FormDefinitionClient.embeddedDischargeSummary();
        }
        if (KEY_WHO_CHECKLIST.equals(key) || PURPOSE_WHO_CHECKLIST.equals(p)) {
            return FormDefinitionClient.embeddedWhoSurgicalChecklist();
        }
        return FormDefinitionClient.embeddedNursingAssessment();
    }

    private String resolveKey(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("purpose is required (ASSESSMENT|CONSENT|DISCHARGE_SUMMARY|WHO_CHECKLIST)");
        }
        return switch (purpose.trim().toUpperCase()) {
            case "CONSENT" -> consentFormKey;
            case "ASSESSMENT" -> assessmentFormKey;
            case PURPOSE_DISCHARGE_SUMMARY -> dischargeSummaryFormKey;
            case PURPOSE_WHO_CHECKLIST -> whoChecklistFormKey;
            default -> purpose.trim();
        };
    }

    @SuppressWarnings("unchecked")
    private void validateMandatory(Map<String, Object> form, Map<String, Object> answers) {
        if (answers == null) {
            throw new IllegalArgumentException("answers are required");
        }
        Object sectionsObj = form.get("sections");
        if (!(sectionsObj instanceof List<?> sections)) {
            return;
        }
        for (Object sectionObj : sections) {
            if (!(sectionObj instanceof Map<?, ?> section)) {
                continue;
            }
            Object fieldsObj = section.get("fields");
            if (!(fieldsObj instanceof List<?> fields)) {
                continue;
            }
            for (Object fieldObj : fields) {
                if (!(fieldObj instanceof Map<?, ?> field)) {
                    continue;
                }
                boolean mandatory = Boolean.TRUE.equals(field.get("mandatory"));
                if (!mandatory) {
                    continue;
                }
                String key = String.valueOf(field.get("key"));
                Object val = answers.get(key);
                if (val == null || String.valueOf(val).isBlank() || Boolean.FALSE.equals(val)) {
                    throw new IllegalArgumentException("Mandatory field missing: " + field.get("label"));
                }
            }
        }
    }

    private IpdAdmission assertAdmission(Long admissionId) {
        return admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid JSON payload");
        }
    }
}
