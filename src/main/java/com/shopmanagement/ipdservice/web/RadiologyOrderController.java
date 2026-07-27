package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.radiology.RadiologyOrder;
import com.shopmanagement.ipdservice.radiology.RadiologyOrderService;

@RestController
@RequestMapping("/ipd")
public class RadiologyOrderController {

    private final RadiologyOrderService radiologyOrderService;

    public RadiologyOrderController(RadiologyOrderService radiologyOrderService) {
        this.radiologyOrderService = radiologyOrderService;
    }

    @GetMapping("/admissions/{admissionId}/radiology-orders")
    public List<RadiologyOrder> list(@PathVariable Long admissionId) {
        return radiologyOrderService.listForAdmission(admissionId);
    }

    @PostMapping("/admissions/{admissionId}/radiology-orders")
    public RadiologyOrder create(@PathVariable Long admissionId, @RequestBody RadiologyOrder body) {
        return radiologyOrderService.create(admissionId, body);
    }

    @PostMapping("/radiology-orders/{id}/advance")
    public RadiologyOrder advance(@PathVariable Long id, @RequestBody AdvanceRequest body) {
        return radiologyOrderService.advance(id, body.status(), body.reportText());
    }

    public record AdvanceRequest(String status, String reportText) {}
}
