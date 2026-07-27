package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.catalogue.CodeCatalogueEntry;
import com.shopmanagement.ipdservice.catalogue.CodeCatalogueService;

@RestController
@RequestMapping("/ipd/catalogues")
public class CodeCatalogueController {

    private final CodeCatalogueService service;

    public CodeCatalogueController(CodeCatalogueService service) {
        this.service = service;
    }

    @GetMapping("/icd")
    public List<CodeCatalogueEntry> searchIcd(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        return service.searchIcd(q, limit);
    }

    @PostMapping
    public CodeCatalogueEntry upsert(@RequestBody CodeCatalogueEntry body) {
        return service.upsert(body);
    }
}
