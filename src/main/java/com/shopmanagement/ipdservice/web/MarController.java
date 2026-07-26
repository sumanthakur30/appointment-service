package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.mar.MarAdministration;
import com.shopmanagement.ipdservice.mar.MarOrder;
import com.shopmanagement.ipdservice.mar.MarService;

@RestController
@RequestMapping("/ipd/mar")
public class MarController {

    private final MarService marService;

    public MarController(MarService marService) {
        this.marService = marService;
    }

    @GetMapping("/admissions/{admissionId}/orders")
    public List<MarOrder> orders(@PathVariable Long admissionId) {
        return marService.listOrders(admissionId);
    }

    @PostMapping("/admissions/{admissionId}/orders")
    public MarOrder create(@PathVariable Long admissionId, @RequestBody MarOrder body) {
        return marService.createOrder(admissionId, body);
    }

    @PostMapping("/orders/{orderId}/stop")
    public MarOrder stop(@PathVariable Long orderId) {
        return marService.stopOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/administer")
    public MarAdministration administer(@PathVariable Long orderId, @RequestBody MarAdministration body) {
        return marService.administer(orderId, body);
    }

    @GetMapping("/admissions/{admissionId}/administrations")
    public List<MarAdministration> administrations(@PathVariable Long admissionId) {
        return marService.listAdministrations(admissionId);
    }
}
