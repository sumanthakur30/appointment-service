package com.shopmanagement.ipdservice.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.quality.QualityIncident;
import com.shopmanagement.ipdservice.quality.QualityIncidentService;

@RestController
@RequestMapping("/ipd/quality")
public class QualityIncidentController {

    private final QualityIncidentService qualityIncidentService;

    public QualityIncidentController(QualityIncidentService qualityIncidentService) {
        this.qualityIncidentService = qualityIncidentService;
    }

    @GetMapping("/incidents")
    public List<QualityIncident> list() {
        return qualityIncidentService.list();
    }

    @PostMapping("/incidents")
    public QualityIncident create(@RequestBody QualityIncident body) {
        return qualityIncidentService.create(body);
    }

    @PostMapping("/incidents/{id}/close")
    public QualityIncident close(@PathVariable Long id, @RequestBody(required = false) CloseRequest body) {
        return qualityIncidentService.close(id, body == null ? null : body.capaNotes());
    }

    @GetMapping("/indicators")
    public Map<String, Object> indicators() {
        return qualityIncidentService.indicatorSnapshot();
    }

    public record CloseRequest(String capaNotes) {}
}
