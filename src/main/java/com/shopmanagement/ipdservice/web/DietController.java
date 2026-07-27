package com.shopmanagement.ipdservice.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.diet.DietPlan;
import com.shopmanagement.ipdservice.diet.DietService;

@RestController
@RequestMapping("/ipd/diet")
public class DietController {

    private final DietService dietService;

    public DietController(DietService dietService) {
        this.dietService = dietService;
    }

    @GetMapping("/admissions/{admissionId}/active")
    public DietPlan active(@PathVariable Long admissionId) {
        return dietService.active(admissionId);
    }

    @GetMapping("/admissions/{admissionId}/history")
    public List<DietPlan> history(@PathVariable Long admissionId) {
        return dietService.history(admissionId);
    }

    @GetMapping("/admissions/{admissionId}/allergy-check")
    public Map<String, Object> allergyCheck(@PathVariable Long admissionId) {
        return dietService.checkAllergyConflicts(admissionId);
    }

    @PostMapping("/admissions/{admissionId}")
    public DietPlan upsert(@PathVariable Long admissionId, @RequestBody DietPlan body) {
        return dietService.upsertActive(admissionId, body);
    }

    @GetMapping("/kitchen/board")
    public List<Map<String, Object>> kitchenBoard() {
        return dietService.kitchenBoard();
    }

    @PostMapping("/kitchen/{dietPlanId}/status")
    public DietPlan kitchenStatus(@PathVariable Long dietPlanId, @RequestBody Map<String, String> body) {
        return dietService.advanceKitchen(dietPlanId, body.get("status"));
    }
}
