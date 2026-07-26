package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.er.ErTriage;
import com.shopmanagement.ipdservice.er.ErTriageService;

@RestController
@RequestMapping("/ipd/er")
public class ErTriageController {

    private final ErTriageService erTriageService;

    public ErTriageController(ErTriageService erTriageService) {
        this.erTriageService = erTriageService;
    }

    @GetMapping("/board")
    public List<ErTriage> board() {
        return erTriageService.board();
    }

    @PostMapping("/triage")
    public ErTriage register(@RequestBody ErTriage body) {
        return erTriageService.register(body);
    }

    @PostMapping("/triage/{id}/status")
    public ErTriage status(@PathVariable Long id, @RequestBody StatusRequest body) {
        return erTriageService.advance(id, body.status());
    }

    @PostMapping("/triage/{id}/admit")
    public ErTriage admit(@PathVariable Long id) {
        return erTriageService.admitToIpd(id);
    }

    public record StatusRequest(String status) {}
}
