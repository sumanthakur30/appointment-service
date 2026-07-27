package com.shopmanagement.ipdservice.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.clinical.IpdAdmission;
import com.shopmanagement.ipdservice.nursing.CriticalLabAlert;
import com.shopmanagement.ipdservice.nursing.CriticalLabPagingService;
import com.shopmanagement.ipdservice.nursing.NursingIntakeOutput;
import com.shopmanagement.ipdservice.nursing.NursingNote;
import com.shopmanagement.ipdservice.nursing.NursingService;
import com.shopmanagement.ipdservice.nursing.NursingVital;

@RestController
@RequestMapping("/ipd/nursing")
public class NursingController {

    private final NursingService nursingService;
    private final CriticalLabPagingService criticalLabPagingService;

    public NursingController(NursingService nursingService, CriticalLabPagingService criticalLabPagingService) {
        this.nursingService = nursingService;
        this.criticalLabPagingService = criticalLabPagingService;
    }

    @GetMapping("/ward")
    public List<IpdAdmission> ward() {
        return nursingService.wardCensus();
    }

    @GetMapping("/handovers")
    public List<NursingNote> handovers() {
        return nursingService.handoverBoard();
    }

    @GetMapping("/handover-board")
    public Map<String, Object> shiftHandoverBoard() {
        return nursingService.shiftHandoverBoard();
    }

    @GetMapping("/config")
    public Map<String, Object> nursingConfig() {
        return Map.of("handoverBoardEnabled", nursingService.isHandoverBoardEnabled());
    }

    @PostMapping("/admissions/{admissionId}/handover")
    public NursingNote handover(@PathVariable Long admissionId, @RequestBody NursingNote body) {
        return nursingService.createHandover(admissionId, body);
    }

    @GetMapping("/critical-labs")
    public List<CriticalLabAlert> criticalLabs() {
        return criticalLabPagingService.openAlerts();
    }

    @GetMapping("/admissions/{admissionId}/critical-labs")
    public List<CriticalLabAlert> criticalLabsForAdmission(@PathVariable Long admissionId) {
        return criticalLabPagingService.forAdmission(admissionId);
    }

    @PostMapping("/critical-labs/refresh")
    public Map<String, Object> refreshCriticalLabs() {
        return criticalLabPagingService.refreshFromLabs();
    }

    @PostMapping("/critical-labs/{id}/ack")
    public CriticalLabAlert ackCriticalLab(@PathVariable Long id) {
        return criticalLabPagingService.acknowledge(id);
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
