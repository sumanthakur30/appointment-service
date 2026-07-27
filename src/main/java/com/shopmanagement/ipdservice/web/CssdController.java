package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.cssd.CssdCycle;
import com.shopmanagement.ipdservice.cssd.CssdService;
import com.shopmanagement.ipdservice.cssd.CssdSet;

@RestController
@RequestMapping("/ipd/cssd")
public class CssdController {

    private final CssdService cssdService;

    public CssdController(CssdService cssdService) {
        this.cssdService = cssdService;
    }

    @GetMapping("/sets")
    public List<CssdSet> sets() {
        return cssdService.listSets();
    }

    @PostMapping("/sets")
    public CssdSet register(@RequestBody CssdSet body) {
        return cssdService.registerSet(body);
    }

    @GetMapping("/cycles")
    public List<CssdCycle> cycles() {
        return cssdService.listCycles();
    }

    @PostMapping("/sets/{setId}/cycles")
    public CssdCycle startCycle(@PathVariable Long setId, @RequestBody(required = false) CssdCycle body) {
        return cssdService.startCycle(setId, body);
    }

    @PostMapping("/cycles/{cycleId}/complete")
    public CssdCycle complete(@PathVariable Long cycleId) {
        return cssdService.completeCycle(cycleId);
    }

    @PostMapping("/sets/{setId}/issue-to-ot")
    public CssdSet issueToOt(@PathVariable Long setId, @RequestBody IssueRequest body) {
        return cssdService.issueToOt(setId, body == null ? null : body.otBookingId());
    }

    @PostMapping("/sets/{setId}/return")
    public CssdSet returnFromOt(@PathVariable Long setId) {
        return cssdService.returnFromOt(setId);
    }

    public record IssueRequest(Long otBookingId) {}
}
