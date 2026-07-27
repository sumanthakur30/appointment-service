package com.shopmanagement.ipdservice.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.cpoe.CpoeOrderSet;
import com.shopmanagement.ipdservice.cpoe.CpoeOrderSetService;

@RestController
@RequestMapping("/ipd/cpoe")
public class CpoeOrderSetController {

    private final CpoeOrderSetService service;

    public CpoeOrderSetController(CpoeOrderSetService service) {
        this.service = service;
    }

    @GetMapping("/order-sets")
    public List<CpoeOrderSet> list() {
        return service.list();
    }

    @PostMapping("/order-sets")
    public CpoeOrderSet upsert(@RequestBody CpoeOrderSet body) {
        return service.upsert(body);
    }

    @PostMapping("/admissions/{admissionId}/order-sets/{code}/apply")
    public Map<String, Object> apply(@PathVariable Long admissionId, @PathVariable String code) {
        return service.apply(admissionId, code);
    }
}
