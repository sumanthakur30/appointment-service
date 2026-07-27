package com.shopmanagement.ipdservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.ipdservice.hl7.Hl7AdtService;
import com.shopmanagement.ipdservice.hl7.Hl7Message;

@RestController
@RequestMapping("/ipd/hl7")
public class Hl7Controller {

    private final Hl7AdtService hl7AdtService;

    public Hl7Controller(Hl7AdtService hl7AdtService) {
        this.hl7AdtService = hl7AdtService;
    }

    @GetMapping("/messages")
    public List<Hl7Message> messages() {
        return hl7AdtService.list();
    }

    @GetMapping("/enabled")
    public boolean enabled() {
        return hl7AdtService.isEnabled();
    }
}
