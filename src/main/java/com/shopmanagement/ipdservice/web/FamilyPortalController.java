package com.shopmanagement.ipdservice.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.family.FamilyPortalService;
import com.shopmanagement.ipdservice.family.VisitorPass;

@RestController
@RequestMapping("/ipd/family")
public class FamilyPortalController {

    private final FamilyPortalService familyPortalService;

    public FamilyPortalController(FamilyPortalService familyPortalService) {
        this.familyPortalService = familyPortalService;
    }

    @GetMapping("/admissions/{admissionId}/passes")
    public List<VisitorPass> passes(@PathVariable Long admissionId) {
        return familyPortalService.listForAdmission(admissionId);
    }

    @PostMapping("/passes")
    public VisitorPass issue(@RequestBody VisitorPass body) {
        return familyPortalService.issue(body);
    }

    @PostMapping("/passes/{id}/revoke")
    public VisitorPass revoke(@PathVariable Long id) {
        return familyPortalService.revoke(id);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestParam String passCode) {
        return familyPortalService.lookupByPass(passCode);
    }

    /** Public family portal — no auth tenant required; pass code is the credential. */
    @GetMapping("/public/dashboard")
    public Map<String, Object> publicDashboard(@RequestParam String passCode) {
        return familyPortalService.publicLookupByPass(passCode);
    }

    @GetMapping("/passes/{id}/share")
    public Map<String, Object> share(@PathVariable Long id) {
        return familyPortalService.passShareInfo(id);
    }

    @GetMapping("/passes/{id}/qr.png")
    public ResponseEntity<byte[]> qr(@PathVariable Long id) {
        byte[] png = familyPortalService.passQrPng(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"visitor-pass-qr.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    @GetMapping("/public/qr.png")
    public ResponseEntity<byte[]> publicQr(@RequestParam String passCode) {
        byte[] png = familyPortalService.publicPassQrPng(passCode);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"visitor-pass-qr.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
