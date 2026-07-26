package com.shopmanagement.ipdservice.clinical;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.ipdservice.support.TenantContext;

@Service
public class PatientClinicalProfileService {

    private final PatientAllergyRepository allergyRepository;
    private final PatientProblemRepository problemRepository;

    public PatientClinicalProfileService(
            PatientAllergyRepository allergyRepository,
            PatientProblemRepository problemRepository) {
        this.allergyRepository = allergyRepository;
        this.problemRepository = problemRepository;
    }

    public List<PatientAllergy> listAllergies(Long patientId) {
        requirePatient(patientId);
        return allergyRepository.findByTenantIdAndShopIdAndPatientIdOrderByNotedAtDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), patientId);
    }

    @Transactional
    public PatientAllergy addAllergy(Long patientId, PatientAllergy incoming) {
        requirePatient(patientId);
        if (incoming.getSubstance() == null || incoming.getSubstance().isBlank()) {
            throw new IllegalArgumentException("substance is required");
        }
        PatientAllergy row = new PatientAllergy();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setPatientId(patientId);
        row.setSubstance(incoming.getSubstance().trim());
        row.setReaction(incoming.getReaction());
        row.setSeverity(normalize(incoming.getSeverity(), "MODERATE"));
        row.setStatus(normalize(incoming.getStatus(), "ACTIVE"));
        row.setNotedAt(incoming.getNotedAt() != null ? incoming.getNotedAt() : LocalDateTime.now());
        row.setRecordedBy(TenantContext.currentActor());
        row.setNotes(incoming.getNotes());
        return allergyRepository.save(row);
    }

    @Transactional
    public PatientAllergy resolveAllergy(Long patientId, Long allergyId) {
        PatientAllergy row = allergyRepository
                .findByIdAndTenantIdAndShopId(allergyId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found"));
        if (!patientId.equals(row.getPatientId())) {
            throw new IllegalArgumentException("Allergy does not belong to patient");
        }
        row.setStatus("RESOLVED");
        return allergyRepository.save(row);
    }

    public List<PatientProblem> listProblems(Long patientId) {
        requirePatient(patientId);
        return problemRepository.findByTenantIdAndShopIdAndPatientIdOrderByIdDesc(
                TenantContext.requireTenantId(), TenantContext.requireShopId(), patientId);
    }

    @Transactional
    public PatientProblem addProblem(Long patientId, PatientProblem incoming) {
        requirePatient(patientId);
        if (incoming.getProblem() == null || incoming.getProblem().isBlank()) {
            throw new IllegalArgumentException("problem is required");
        }
        PatientProblem row = new PatientProblem();
        row.setTenantId(TenantContext.requireTenantId());
        row.setShopId(TenantContext.requireShopId());
        row.setPatientId(patientId);
        row.setProblem(incoming.getProblem().trim());
        row.setStatus(normalize(incoming.getStatus(), "ACTIVE"));
        row.setOnsetDate(incoming.getOnsetDate());
        row.setRecordedBy(TenantContext.currentActor());
        row.setNotes(incoming.getNotes());
        return problemRepository.save(row);
    }

    @Transactional
    public PatientProblem resolveProblem(Long patientId, Long problemId) {
        PatientProblem row = problemRepository
                .findByIdAndTenantIdAndShopId(problemId, TenantContext.requireTenantId(), TenantContext.requireShopId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        if (!patientId.equals(row.getPatientId())) {
            throw new IllegalArgumentException("Problem does not belong to patient");
        }
        row.setStatus("RESOLVED");
        return problemRepository.save(row);
    }

    private static void requirePatient(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("patientId is required");
        }
    }

    private static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
