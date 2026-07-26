package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.clinical.PatientAllergy;
import com.shopmanagement.ipdservice.clinical.PatientClinicalProfileService;
import com.shopmanagement.ipdservice.clinical.PatientProblem;

@RestController
@RequestMapping("/ipd/patients/{patientId}")
public class PatientClinicalProfileController {

    private final PatientClinicalProfileService clinicalProfileService;

    public PatientClinicalProfileController(PatientClinicalProfileService clinicalProfileService) {
        this.clinicalProfileService = clinicalProfileService;
    }

    @GetMapping("/allergies")
    public List<PatientAllergy> allergies(@PathVariable Long patientId) {
        return clinicalProfileService.listAllergies(patientId);
    }

    @PostMapping("/allergies")
    public PatientAllergy addAllergy(@PathVariable Long patientId, @RequestBody PatientAllergy body) {
        return clinicalProfileService.addAllergy(patientId, body);
    }

    @PostMapping("/allergies/{allergyId}/resolve")
    public PatientAllergy resolveAllergy(@PathVariable Long patientId, @PathVariable Long allergyId) {
        return clinicalProfileService.resolveAllergy(patientId, allergyId);
    }

    @GetMapping("/problems")
    public List<PatientProblem> problems(@PathVariable Long patientId) {
        return clinicalProfileService.listProblems(patientId);
    }

    @PostMapping("/problems")
    public PatientProblem addProblem(@PathVariable Long patientId, @RequestBody PatientProblem body) {
        return clinicalProfileService.addProblem(patientId, body);
    }

    @PostMapping("/problems/{problemId}/resolve")
    public PatientProblem resolveProblem(@PathVariable Long patientId, @PathVariable Long problemId) {
        return clinicalProfileService.resolveProblem(patientId, problemId);
    }
}
