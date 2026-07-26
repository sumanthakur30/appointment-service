package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.nursing.NursingIntakeOutput;
import com.shopmanagement.ipdservice.nursing.NursingNote;
import com.shopmanagement.ipdservice.nursing.NursingService;
import com.shopmanagement.ipdservice.nursing.NursingVital;

@RestController
@RequestMapping("/ipd/nursing")
public class NursingController {

    private final NursingService nursingService;

    public NursingController(NursingService nursingService) {
        this.nursingService = nursingService;
    }

    @GetMapping("/ward")
    public List<IpdAdmission> ward() {
        return nursingService.wardCensus();
    }

    @GetMapping("/admissions/{admissionId}/vitals")
    public List<NursingVital> vitals(@PathVariable Long admissionId) {
        return nursingService.listVitals(admissionId);
    }

    @PostMapping("/admissions/{admissionId}/vitals")
    public NursingVital recordVital(@PathVariable Long admissionId, @RequestBody NursingVital body) {
        return nursingService.recordVital(admissionId, body);
    }

    @GetMapping("/admissions/{admissionId}/io")
    public List<NursingIntakeOutput> io(@PathVariable Long admissionId) {
        return nursingService.listIo(admissionId);
    }

    @PostMapping("/admissions/{admissionId}/io")
    public NursingIntakeOutput recordIo(@PathVariable Long admissionId, @RequestBody NursingIntakeOutput body) {
        return nursingService.recordIo(admissionId, body);
    }

    @GetMapping("/admissions/{admissionId}/notes")
    public List<NursingNote> notes(@PathVariable Long admissionId) {
        return nursingService.listNotes(admissionId);
    }

    @PostMapping("/admissions/{admissionId}/notes")
    public NursingNote addNote(@PathVariable Long admissionId, @RequestBody NursingNote body) {
        return nursingService.addNote(admissionId, body);
    }
}
