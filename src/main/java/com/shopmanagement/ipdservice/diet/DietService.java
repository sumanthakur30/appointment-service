package com.shopmanagement.ipdservice.diet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.clinical.IpdAdmissionRepository;
import com.shopmanagement.ipdservice.clinical.PatientAllergy;
import com.shopmanagement.ipdservice.clinical.PatientClinicalProfileService;
import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class DietService {

    private static final Set<String> KITCHEN = Set.of("ORDERED", "PREPARING", "READY", "SERVED", "CANCELLED");

    private final DietPlanRepository dietPlanRepository;
    private final IpdAdmissionRepository admissionRepository;
    private final PatientClinicalProfileService clinicalProfileService;
    private final boolean kitchenEnabled;

    public DietService(
            DietPlanRepository dietPlanRepository,
            IpdAdmissionRepository admissionRepository,
            PatientClinicalProfileService clinicalProfileService,
            @Value("${ipd.diet.kitchen-enabled:true}") boolean kitchenEnabled) {
        this.dietPlanRepository = dietPlanRepository;
        this.admissionRepository = admissionRepository;
        this.clinicalProfileService = clinicalProfileService;
        this.kitchenEnabled = kitchenEnabled;
    }

    @Transactional
    public DietPlan upsertActive(Long admissionId, DietPlan incoming) {
        IpdAdmission admission = admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));

        dietPlanRepository.findFirstByTenantIdAndShopIdAndAdmissionIdAndActiveTrueOrderByEffectiveFromDesc(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    dietPlanRepository.save(existing);
                });

        DietPlan p = new DietPlan();
        p.setTenantId(TenantContext.requireTenantId());
        p.setShopId(TenantContext.requireShopId());
        p.setAdmissionId(admissionId);
        p.setDietType(incoming.getDietType() == null || incoming.getDietType().isBlank()
                ? "REGULAR" : incoming.getDietType().trim().toUpperCase());
        p.setFluidRestrictionMl(incoming.getFluidRestrictionMl());
        p.setBreakfast(incoming.getBreakfast());
        p.setLunch(incoming.getLunch());
        p.setDinner(incoming.getDinner());
        String notes = incoming.getSpecialNotes();
        List<String> conflicts = allergyConflicts(admission.getPatientId(), p);
        if (!conflicts.isEmpty()) {
            String warn = "Allergy/diet conflict: " + String.join("; ", conflicts);
            notes = (notes == null || notes.isBlank() ? warn : notes + "\n" + warn);
        }
        p.setSpecialNotes(notes);
        p.setActive(true);
        p.setKitchenStatus("ORDERED");
        p.setDietician(incoming.getDietician() != null ? incoming.getDietician() : TenantContext.currentActor());
        p.setEffectiveFrom(incoming.getEffectiveFrom() != null ? incoming.getEffectiveFrom() : LocalDate.now());
        return dietPlanRepository.save(p);
    }

    public DietPlan active(Long admissionId) {
        return dietPlanRepository.findFirstByTenantIdAndShopIdAndAdmissionIdAndActiveTrueOrderByEffectiveFromDesc(
                        TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId)
                .orElse(null);
    }

    public List<DietPlan> history(Long admissionId) {
        return dietPlanRepository.findByTenantIdAndShopIdAndAdmissionIdOrderByEffectiveFromDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), admissionId);
    }

    public List<Map<String, Object>> kitchenBoard() {
        if (!kitchenEnabled) {
            return List.of();
        }
        Long tenantId = TenantContext.requireTenantId();
        String shopId = TenantContext.requireShopId();
        List<DietPlan> plans = dietPlanRepository.findByTenantIdAndShopIdAndActiveTrueOrderByEffectiveFromDesc(
                tenantId, shopId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (DietPlan plan : plans) {
            IpdAdmission admission = admissionRepository.findByIdAndTenantIdAndShopId(
                    plan.getAdmissionId(), tenantId, shopId).orElse(null);
            if (admission == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("dietPlanId", plan.getId());
            row.put("admissionId", admission.getId());
            row.put("admissionNo", admission.getAdmissionNo());
            row.put("patientName", admission.getPatientName());
            row.put("patientId", admission.getPatientId());
            row.put("dietType", plan.getDietType());
            row.put("kitchenStatus", plan.getKitchenStatus());
            row.put("breakfast", plan.getBreakfast());
            row.put("lunch", plan.getLunch());
            row.put("dinner", plan.getDinner());
            row.put("specialNotes", plan.getSpecialNotes());
            row.put("allergyConflicts", allergyConflicts(admission.getPatientId(), plan));
            out.add(row);
        }
        return out;
    }

    @Transactional
    public DietPlan advanceKitchen(Long dietPlanId, String status) {
        if (!kitchenEnabled) {
            throw new IllegalStateException("Diet kitchen board disabled");
        }
        String s = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!KITCHEN.contains(s)) {
            throw new IllegalArgumentException("Invalid kitchen status; use " + KITCHEN);
        }
        DietPlan plan = dietPlanRepository.findByIdAndTenantIdAndShopId(
                        dietPlanId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Diet plan not found"));
        plan.setKitchenStatus(s);
        if ("SERVED".equals(s) || "READY".equals(s)) {
            plan.setTrayAckedAt(LocalDateTime.now());
            plan.setTrayAckedBy(TenantContext.currentActor());
        }
        return dietPlanRepository.save(plan);
    }

    public Map<String, Object> checkAllergyConflicts(Long admissionId) {
        IpdAdmission admission = admissionRepository.findByIdAndTenantIdAndShopId(
                        admissionId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
        DietPlan plan = active(admissionId);
        List<String> conflicts = plan == null ? List.of() : allergyConflicts(admission.getPatientId(), plan);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("admissionId", admissionId);
        out.put("conflicts", conflicts);
        out.put("hasConflict", !conflicts.isEmpty());
        return out;
    }

    private List<String> allergyConflicts(Long patientId, DietPlan plan) {
        if (patientId == null || plan == null) {
            return List.of();
        }
        List<PatientAllergy> allergies = clinicalProfileService.listAllergies(patientId).stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .toList();
        if (allergies.isEmpty()) {
            return List.of();
        }
        String blob = ((plan.getBreakfast() == null ? "" : plan.getBreakfast()) + " "
                + (plan.getLunch() == null ? "" : plan.getLunch()) + " "
                + (plan.getDinner() == null ? "" : plan.getDinner()) + " "
                + (plan.getSpecialNotes() == null ? "" : plan.getSpecialNotes()) + " "
                + (plan.getDietType() == null ? "" : plan.getDietType()))
                .toLowerCase(Locale.ROOT);
        List<String> hits = new ArrayList<>();
        for (PatientAllergy allergy : allergies) {
            String substance = allergy.getSubstance() == null ? "" : allergy.getSubstance().trim().toLowerCase(Locale.ROOT);
            if (substance.length() < 3) {
                continue;
            }
            if (blob.contains(substance)) {
                hits.add(allergy.getSubstance());
            }
        }
        return hits;
    }
}
