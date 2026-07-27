package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.infection.HaiEvent;
import com.shopmanagement.ipdservice.infection.InfectionControlService;
import com.shopmanagement.ipdservice.infection.InfectionIsolation;

@RestController
@RequestMapping("/ipd/infection")
public class InfectionController {

    private final InfectionControlService infectionControlService;

    public InfectionController(InfectionControlService infectionControlService) {
        this.infectionControlService = infectionControlService;
    }

    @GetMapping("/active")
    public List<InfectionIsolation> active() {
        return infectionControlService.listActive();
    }

    @GetMapping("/admissions/{admissionId}")
    public List<InfectionIsolation> forAdmission(@PathVariable Long admissionId) {
        return infectionControlService.listForAdmission(admissionId);
    }

    @PostMapping
    public InfectionIsolation start(@RequestBody InfectionIsolation body) {
        return infectionControlService.start(body);
    }

    @PostMapping("/{id}/end")
    public InfectionIsolation end(@PathVariable Long id) {
        return infectionControlService.end(id);
    }

    @GetMapping("/hai")
    public List<HaiEvent> hai(@RequestParam(required = false) String status) {
        return infectionControlService.listHai(status);
    }

    @PostMapping("/hai")
    public HaiEvent reportHai(@RequestBody HaiEvent body) {
        return infectionControlService.reportHai(body);
    }

    @PostMapping("/hai/{id}/close")
    public HaiEvent closeHai(@PathVariable Long id) {
        return infectionControlService.closeHai(id);
    }
}
