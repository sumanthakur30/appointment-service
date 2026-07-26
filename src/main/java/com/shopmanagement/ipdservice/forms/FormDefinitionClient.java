package com.shopmanagement.ipdservice.forms;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.shopmanagement.ipdservice.filter.RequestIdFilter;

/**
 * Optional client to school form-builder-service ({@code GET /api/forms/{formKey}}).
 * Falls back to embedded platform defaults when unreachable.
 */
@Component
public class FormDefinitionClient {

    private static final Logger log = LoggerFactory.getLogger(FormDefinitionClient.class);

    private final WebClient webClient;
    private final boolean enabled;

    public FormDefinitionClient(
            WebClient.Builder builder,
            @Value("${forms.service.base-url:http://localhost:8183}") String baseUrl,
            @Value("${forms.service.enabled:true}") boolean enabled) {
        this.webClient = builder.baseUrl(trimSlash(baseUrl)).build();
        this.enabled = enabled;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchForm(String formKey) {
        if (!enabled || formKey == null || formKey.isBlank()) {
            return null;
        }
        try {
            Map<?, ?> body = webClient.get()
                    .uri("/api/forms/{key}", formKey.trim())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(RequestIdFilter.TENANT_ID_HEADER,
                            RequestIdFilter.getCurrentTenantId() != null
                                    ? String.valueOf(RequestIdFilter.getCurrentTenantId()) : "0")
                    .header(RequestIdFilter.SHOP_ID_HEADER,
                            RequestIdFilter.getCurrentShopId() != null
                                    ? RequestIdFilter.getCurrentShopId() : "default")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(8))
                    .block();
            return body == null ? null : new LinkedHashMap<>((Map<String, Object>) body);
        } catch (WebClientResponseException.NotFound ex) {
            return null;
        } catch (Exception ex) {
            log.debug("form-builder unavailable for {}: {}", formKey, ex.getMessage());
            return null;
        }
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8183";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static Map<String, Object> embeddedNursingAssessment() {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formKey", "ipd_nursing_assessment");
        form.put("title", "IPD Nursing Daily Assessment");
        form.put("sections", List.of(Map.of(
                "id", "assessment",
                "title", "Assessment",
                "repeatable", false,
                "fields", List.of(
                        field("consciousness", "Level of consciousness", "DROPDOWN", true),
                        field("mobility", "Mobility", "DROPDOWN", true),
                        field("skinIntegrity", "Skin integrity", "DROPDOWN", true),
                        field("pressureUlcerRisk", "Pressure ulcer risk", "DROPDOWN", false),
                        field("fallRisk", "Fall risk", "DROPDOWN", true),
                        field("painScore", "Pain score (0-10)", "NUMBER", true),
                        field("ivSiteOk", "IV site satisfactory", "CHECKBOX", false),
                        field("notes", "Nurse notes", "TEXTAREA", false)
                ))));
        return form;
    }

    public static Map<String, Object> embeddedAdmissionConsent() {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formKey", "ipd_admission_consent");
        form.put("title", "IPD Admission Consent");
        form.put("sections", List.of(Map.of(
                "id", "consent",
                "title", "Consent",
                "repeatable", false,
                "fields", List.of(
                        field("patientOrGuardian", "Patient / Guardian name", "TEXTBOX", true),
                        field("relation", "Relation to patient", "TEXTBOX", true),
                        field("understoodTreatment", "I understand the proposed treatment", "CHECKBOX", true),
                        field("consentSurgery", "Consent for procedures / surgery if advised", "CHECKBOX", false),
                        field("consentData", "Consent to process health data", "CHECKBOX", true),
                        field("signatureName", "Signature (type full name)", "TEXTBOX", true),
                        field("signedAt", "Signed date", "DATE", true)
                ))));
        return form;
    }

    public static Map<String, Object> embeddedDischargeSummary() {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formKey", "ipd_discharge_summary");
        form.put("title", "IPD Discharge Summary");
        form.put("sections", List.of(
                Map.of(
                        "id", "clinical",
                        "title", "Clinical summary",
                        "repeatable", false,
                        "fields", List.of(
                                field("finalDiagnosis", "Final diagnosis", "TEXTBOX", true),
                                field("secondaryDiagnosis", "Secondary / comorbidities", "TEXTAREA", false),
                                field("courseInHospital", "Course in hospital", "TEXTAREA", true),
                                field("procedures", "Procedures / surgeries", "TEXTAREA", false),
                                field("investigations", "Key investigations", "TEXTAREA", false)
                        )),
                Map.of(
                        "id", "advice",
                        "title", "Advice & follow-up",
                        "repeatable", false,
                        "fields", List.of(
                                field("conditionAtDischarge", "Condition at discharge", "DROPDOWN", true),
                                field("medications", "Discharge medications", "TEXTAREA", true),
                                field("dietAdvice", "Diet advice", "TEXTAREA", false),
                                field("activityAdvice", "Activity / precautions", "TEXTAREA", false),
                                field("followUp", "Follow-up instructions", "TEXTAREA", true),
                                field("doctorName", "Discharging doctor", "TEXTBOX", true),
                                field("signedAt", "Summary date", "DATE", true)
                        ))
        ));
        return form;
    }

    public static Map<String, Object> embeddedWhoSurgicalChecklist() {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formKey", "ipd_who_surgical_checklist");
        form.put("title", "WHO Surgical Safety Checklist");
        form.put("sections", List.of(
                Map.of(
                        "id", "sign_in",
                        "title", "Sign in (before induction)",
                        "repeatable", false,
                        "fields", List.of(
                                field("patientIdentityConfirmed", "Patient identity confirmed", "CHECKBOX", true),
                                field("siteMarked", "Surgical site marked", "CHECKBOX", true),
                                field("anaesthesiaSafetyCheck", "Anaesthesia safety check complete", "CHECKBOX", true),
                                field("pulseOximeter", "Pulse oximeter on patient", "CHECKBOX", true),
                                field("knownAllergy", "Known allergy reviewed", "CHECKBOX", true)
                        )),
                Map.of(
                        "id", "time_out",
                        "title", "Time out (before incision)",
                        "repeatable", false,
                        "fields", List.of(
                                field("teamIntroduced", "Team members introduced", "CHECKBOX", true),
                                field("procedureConfirmed", "Procedure confirmed", "CHECKBOX", true),
                                field("antibioticProphylaxis", "Antibiotic prophylaxis given", "CHECKBOX", false),
                                field("imagingDisplayed", "Essential imaging displayed", "CHECKBOX", false)
                        )),
                Map.of(
                        "id", "sign_out",
                        "title", "Sign out (before leaving OT)",
                        "repeatable", false,
                        "fields", List.of(
                                field("instrumentCount", "Instrument / sponge count complete", "CHECKBOX", true),
                                field("specimenLabelled", "Specimen labelled", "CHECKBOX", false),
                                field("equipmentProblems", "Equipment problems noted", "TEXTAREA", false),
                                field("recoveryPlan", "Recovery / concerns discussed", "TEXTAREA", true)
                        ))
        ));
        return form;
    }

    private static Map<String, Object> field(String key, String label, String type, boolean mandatory) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("key", key);
        f.put("label", label);
        f.put("type", type);
        f.put("mandatory", mandatory);
        f.put("showInReports", true);
        return f;
    }
}
