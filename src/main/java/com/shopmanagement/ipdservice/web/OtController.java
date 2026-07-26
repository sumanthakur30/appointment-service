package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.ot.OtBooking;
import com.shopmanagement.ipdservice.ot.OtService;

@RestController
@RequestMapping("/ipd/ot")
public class OtController {

    private final OtService otService;

    public OtController(OtService otService) {
        this.otService = otService;
    }

    @GetMapping("/bookings")
    public List<OtBooking> list() {
        return otService.list();
    }

    @GetMapping("/admissions/{admissionId}/bookings")
    public List<OtBooking> forAdmission(@PathVariable Long admissionId) {
        return otService.listForAdmission(admissionId);
    }

    @PostMapping("/bookings")
    public OtBooking book(@RequestBody OtBooking body) {
        return otService.book(body);
    }

    @PostMapping("/bookings/{id}/advance")
    public OtBooking advance(@PathVariable Long id, @RequestBody AdvanceRequest body) {
        return otService.advance(id, body.status(), body.postopNotes(), body.recoveryBedId());
    }

    public record AdvanceRequest(String status, String postopNotes, Long recoveryBedId) {}
}
