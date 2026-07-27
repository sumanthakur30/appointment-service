package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.blood.BloodBankService;
import com.shopmanagement.ipdservice.blood.BloodRequest;
import com.shopmanagement.ipdservice.blood.BloodUnit;

@RestController
@RequestMapping("/ipd/blood")
public class BloodBankController {

    private final BloodBankService bloodBankService;

    public BloodBankController(BloodBankService bloodBankService) {
        this.bloodBankService = bloodBankService;
    }

    @GetMapping("/units")
    public List<BloodUnit> units() {
        return bloodBankService.listUnits();
    }

    @PostMapping("/units")
    public BloodUnit addUnit(@RequestBody BloodUnit body) {
        return bloodBankService.addUnit(body);
    }

    @GetMapping("/requests")
    public List<BloodRequest> requests() {
        return bloodBankService.listRequests();
    }

    @GetMapping("/admissions/{admissionId}/requests")
    public List<BloodRequest> forAdmission(@PathVariable Long admissionId) {
        return bloodBankService.listForAdmission(admissionId);
    }

    @PostMapping("/requests")
    public BloodRequest create(@RequestBody BloodRequest body) {
        return bloodBankService.createRequest(body);
    }

    @PostMapping("/requests/{id}/advance")
    public BloodRequest advance(@PathVariable Long id, @RequestBody AdvanceRequest body) {
        return bloodBankService.advance(id, body == null ? null : body.status(), body == null ? null : body.unitId());
    }

    public record AdvanceRequest(String status, Long unitId) {}
}
