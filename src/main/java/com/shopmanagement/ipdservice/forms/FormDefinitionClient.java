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
            if (body == null) {
                return null;
            }
            Object data = body.get("data");
            if (data instanceof Map<?, ?> nested) {
                return new LinkedHashMap<>((Map<String, Object>) nested);
            }
            return new LinkedHashMap<>((Map<String, Object>) body);
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
                                field("primaryIcdCode", "Primary ICD-10 code", "TEXTBOX", true),
                                field("primaryIcdDesc", "Primary ICD description", "TEXTBOX", true),
                                field("secondaryIcdCodes", "Secondary ICD codes (comma-separated)", "TEXTBOX", false),
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

    public static Map<String, Object> embeddedShiftHandover() {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formKey", "ipd_shift_handover");
        form.put("title", "Nurse Shift Handover (SBAR)");
        form.put("sections", List.of(Map.of(
                "id", "sbar",
                "title", "SBAR",
                "repeatable", false,
                "fields", List.of(
                        field("fromNurse", "Outgoing nurse", "TEXTBOX", true),
                        field("toNurse", "Incoming nurse", "TEXTBOX", true),
                        field("shiftCode", "Shift (e.g. DAY/NIGHT)", "TEXTBOX", true),
                        field("situation", "Situation", "TEXTAREA", true),
                        field("background", "Background", "TEXTAREA", true),
                        field("assessment", "Assessment", "TEXTAREA", true),
                        field("recommendation", "Recommendation", "TEXTAREA", true),
                        field("pendingTasks", "Pending tasks", "TEXTAREA", false)
                ))));
        return form;
    }

    public static Map<String, Object> embeddedTransferHandoff() {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formKey", "ipd_transfer_handoff");
        form.put("title", "Bed Transfer Clinical Handoff");
        form.put("sections", List.of(Map.of(
                "id", "handoff",
                "title", "Clinical handoff",
                "repeatable", false,
                "fields", List.of(
                        field("identityConfirmed", "Patient identity confirmed", "CHECKBOX", true),
                        field("allergiesReviewed", "Allergies reviewed", "CHECKBOX", true),
                        field("diagnosisSummary", "Diagnosis / reason for transfer", "TEXTAREA", true),
                        field("currentMeds", "Current medications / MAR status", "TEXTAREA", true),
                        field("pendingLabs", "Pending labs / imaging", "TEXTAREA", false),
                        field("isolationStatus", "Isolation / IPC status", "TEXTBOX", false),
                        field("receivingNurse", "Receiving nurse / unit", "TEXTBOX", true),
                        field("specialAlerts", "Special alerts", "TEXTAREA", false)
                ))));
        return form;
    }

    /** Generic / insurer-labelled claim form — swap via form-builder key without code changes. */
    public static Map<String, Object> embeddedTpaClaim(String claimFormat) {
        String format = claimFormat == null || claimFormat.isBlank() ? "GENERIC" : claimFormat.trim().toUpperCase();
        return switch (format) {
            case "STAR_HEALTH" -> starHealthClaim();
            case "NIVA_BUPA" -> nivaBupaClaim();
            default -> genericTpaClaim(format);
        };
    }

    public static Map<String, Object> genericTpaClaim(String format) {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formKey", "ipd_tpa_claim");
        form.put("claimFormat", format);
        form.put("title", "TPA / Cashless Claim (" + format + ")");
        form.put("sections", List.of(
                Map.of(
                        "id", "member",
                        "title", "Member & admission",
                        "repeatable", false,
                        "fields", List.of(
                                field("claimFormat", "Claim format / insurer template", "TEXTBOX", true),
                                field("tpaName", "TPA / Insurer", "TEXTBOX", true),
                                field("preauthRef", "Pre-auth / claim reference", "TEXTBOX", false),
                                field("preauthStatus", "Pre-auth status", "TEXTBOX", true),
                                field("admissionNo", "Admission number", "TEXTBOX", true),
                                field("patientName", "Patient name", "TEXTBOX", true),
                                field("patientId", "Patient ID", "TEXTBOX", true)
                        )),
                Map.of(
                        "id", "clinical",
                        "title", "Clinical coding",
                        "repeatable", false,
                        "fields", List.of(
                                field("diagnosis", "Diagnosis (free text)", "TEXTAREA", false),
                                field("primaryIcdCode", "Primary ICD-10", "TEXTBOX", false),
                                field("primaryIcdDesc", "Primary ICD description", "TEXTBOX", false),
                                field("secondaryIcdCodes", "Secondary ICD codes", "TEXTBOX", false),
                                field("admittedAt", "Admitted at", "TEXTBOX", false),
                                field("dischargedAt", "Discharged at", "TEXTBOX", false)
                        )),
                Map.of(
                        "id", "financial",
                        "title", "Financial",
                        "repeatable", false,
                        "fields", List.of(
                                field("approvedAmount", "Approved amount (₹)", "TEXTBOX", false),
                                field("packageCode", "Package code", "TEXTBOX", false),
                                field("notes", "Claim notes", "TEXTAREA", false)
                        ))
        ));
        return form;
    }

    public static Map<String, Object> starHealthClaim() {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formKey", "ipd_tpa_claim_star_health");
        form.put("claimFormat", "STAR_HEALTH");
        form.put("title", "Star Health Cashless Claim");
        form.put("sections", List.of(
                Map.of(
                        "id", "member",
                        "title", "Star Health member",
                        "repeatable", false,
                        "fields", List.of(
                                field("claimFormat", "Claim format", "TEXTBOX", true),
                                field("tpaName", "Insurer", "TEXTBOX", true),
                                field("starMemberId", "Star Health member ID", "TEXTBOX", true),
                                field("starPolicyNo", "Policy number", "TEXTBOX", true),
                                field("preauthRef", "Cashless auth number", "TEXTBOX", true),
                                field("preauthStatus", "Auth status", "TEXTBOX", true),
                                field("admissionNo", "Hospital UHID / admission no", "TEXTBOX", true),
                                field("patientName", "Patient name", "TEXTBOX", true)
                        )),
                Map.of(
                        "id", "clinical",
                        "title", "Clinical",
                        "repeatable", false,
                        "fields", List.of(
                                field("primaryIcdCode", "Primary ICD-10", "TEXTBOX", true),
                                field("primaryIcdDesc", "ICD description", "TEXTBOX", true),
                                field("diagnosis", "Clinical diagnosis", "TEXTAREA", true),
                                field("secondaryIcdCodes", "Secondary ICD codes", "TEXTBOX", false),
                                field("admittedAt", "DOA", "TEXTBOX", false),
                                field("dischargedAt", "DOD", "TEXTBOX", false)
                        )),
                Map.of(
                        "id", "financial",
                        "title", "Claim amount",
                        "repeatable", false,
                        "fields", List.of(
                                field("approvedAmount", "Approved amount (₹)", "NUMBER", true),
                                field("packageCode", "Package / procedure code", "TEXTBOX", false),
                                field("notes", "Hospital remarks", "TEXTAREA", false)
                        ))
        ));
        return form;
    }

    public static Map<String, Object> nivaBupaClaim() {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("formKey", "ipd_tpa_claim_niva_bupa");
        form.put("claimFormat", "NIVA_BUPA");
        form.put("title", "Niva Bupa Pre-auth / Claim");
        form.put("sections", List.of(
                Map.of(
                        "id", "member",
                        "title", "Niva Bupa membership",
                        "repeatable", false,
                        "fields", List.of(
                                field("claimFormat", "Claim format", "TEXTBOX", true),
                                field("tpaName", "Insurer", "TEXTBOX", true),
                                field("nivaPolicyNo", "Niva Bupa policy no", "TEXTBOX", true),
                                field("nivaMemberId", "Member / beneficiary ID", "TEXTBOX", false),
                                field("preauthRef", "Pre-auth reference", "TEXTBOX", true),
                                field("preauthStatus", "Pre-auth status", "TEXTBOX", true),
                                field("admissionNo", "Admission number", "TEXTBOX", true),
                                field("patientName", "Patient name", "TEXTBOX", true),
                                field("patientId", "Hospital patient ID", "TEXTBOX", false)
                        )),
                Map.of(
                        "id", "clinical",
                        "title", "Clinical coding",
                        "repeatable", false,
                        "fields", List.of(
                                field("primaryIcdCode", "Primary ICD-10", "TEXTBOX", true),
                                field("primaryIcdDesc", "Primary ICD description", "TEXTBOX", true),
                                field("diagnosis", "Diagnosis", "TEXTAREA", true),
                                field("secondaryIcdCodes", "Secondary ICD", "TEXTBOX", false),
                                field("admittedAt", "Admission datetime", "TEXTBOX", false),
                                field("dischargedAt", "Discharge datetime", "TEXTBOX", false)
                        )),
                Map.of(
                        "id", "financial",
                        "title", "Financial",
                        "repeatable", false,
                        "fields", List.of(
                                field("approvedAmount", "Approved / estimated amount (₹)", "NUMBER", true),
                                field("packageCode", "Package code", "TEXTBOX", false),
                                field("notes", "Claim notes", "TEXTAREA", false)
                        ))
        ));
        return form;
    }

    /**
     * Publish insurer templates to form-builder (PUT /api/forms/{key}).
     * Returns false when form-builder is disabled or unreachable.
     */
    @SuppressWarnings("unchecked")
    public boolean publishForm(String formKey, Map<String, Object> form) {
        if (!enabled || formKey == null || formKey.isBlank() || form == null) {
            return false;
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>(form);
            body.put("formKey", formKey.trim());
            webClient.put()
                    .uri("/api/forms/{key}", formKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(RequestIdFilter.TENANT_ID_HEADER,
                            RequestIdFilter.getCurrentTenantId() != null
                                    ? String.valueOf(RequestIdFilter.getCurrentTenantId()) : "0")
                    .header(RequestIdFilter.SHOP_ID_HEADER,
                            RequestIdFilter.getCurrentShopId() != null
                                    ? RequestIdFilter.getCurrentShopId() : "default")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(12))
                    .block();
            return true;
        } catch (Exception ex) {
            log.warn("form-builder publish failed for {}: {}", formKey, ex.getMessage());
            return false;
        }
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
