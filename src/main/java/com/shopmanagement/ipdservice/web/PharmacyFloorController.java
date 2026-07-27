package com.shopmanagement.ipdservice.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.pharmacy.ControlledDrugRegisterEntry;
import com.shopmanagement.ipdservice.pharmacy.ImprestParLevel;
import com.shopmanagement.ipdservice.pharmacy.ImprestTxn;
import com.shopmanagement.ipdservice.pharmacy.PharmacyFloorService;
import com.shopmanagement.ipdservice.pharmacy.WardImprestLocation;

@RestController
@RequestMapping("/ipd/pharmacy")
public class PharmacyFloorController {

    private final PharmacyFloorService pharmacyFloorService;

    public PharmacyFloorController(PharmacyFloorService pharmacyFloorService) {
        this.pharmacyFloorService = pharmacyFloorService;
    }

    @GetMapping("/flags")
    public Map<String, Object> flags() {
        return pharmacyFloorService.flags();
    }

    @GetMapping("/controlled-drugs/register")
    public List<ControlledDrugRegisterEntry> register() {
        return pharmacyFloorService.listRegister();
    }

    @PostMapping("/controlled-drugs/register")
    public ControlledDrugRegisterEntry postRegister(@RequestBody ControlledDrugRegisterEntry body) {
        return pharmacyFloorService.postRegister(body);
    }

    @GetMapping("/imprest/locations")
    public List<WardImprestLocation> locations() {
        return pharmacyFloorService.listLocations();
    }

    @PostMapping("/imprest/locations")
    public WardImprestLocation createLocation(@RequestBody WardImprestLocation body) {
        return pharmacyFloorService.createLocation(body);
    }

    @GetMapping("/imprest/locations/{id}/par")
    public List<ImprestParLevel> par(@PathVariable Long id) {
        return pharmacyFloorService.listPar(id);
    }

    @PostMapping("/imprest/locations/{id}/par")
    public ImprestParLevel upsertPar(@PathVariable Long id, @RequestBody ImprestParLevel body) {
        return pharmacyFloorService.upsertPar(id, body);
    }

    @GetMapping("/imprest/locations/{id}/txns")
    public List<ImprestTxn> txns(@PathVariable Long id) {
        return pharmacyFloorService.listTxns(id);
    }

    @PostMapping("/imprest/locations/{id}/txns")
    public ImprestTxn postTxn(@PathVariable Long id, @RequestBody ImprestTxn body) {
        return pharmacyFloorService.postTxn(id, body);
    }

    @GetMapping("/imprest/locations/{id}/low-stock")
    public List<ImprestParLevel> lowStock(@PathVariable Long id) {
        return pharmacyFloorService.lowStock(id);
    }
}
