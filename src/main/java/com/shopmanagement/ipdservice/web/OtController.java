package com.shopmanagement.ipdservice.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.ot.OtBooking;
import com.shopmanagement.ipdservice.ot.OtImplantUsage;
import com.shopmanagement.ipdservice.ot.OtPreferenceCard;
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

    @GetMapping("/preference-cards")
    public List<OtPreferenceCard> preferenceCards() {
        return otService.listPreferenceCards();
    }

    @PostMapping("/preference-cards")
    public OtPreferenceCard upsertPreferenceCard(@RequestBody OtPreferenceCard body) {
        return otService.upsertPreferenceCard(body);
    }

    @PostMapping("/bookings/{id}/preference-cards/{code}/apply")
    public Map<String, Object> applyPreferenceCard(@PathVariable Long id, @PathVariable String code) {
        return otService.applyPreferenceCard(id, code);
    }

    @GetMapping("/bookings/{id}/implants")
    public List<OtImplantUsage> implants(@PathVariable Long id) {
        return otService.listImplants(id);
    }

    @PostMapping("/bookings/{id}/implants")
    public OtImplantUsage recordImplant(@PathVariable Long id, @RequestBody OtImplantUsage body) {
        return otService.recordImplant(id, body);
    }

    public record AdvanceRequest(String status, String postopNotes, Long recoveryBedId) {}
}
