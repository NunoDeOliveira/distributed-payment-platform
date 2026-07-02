package com.tfg.commissionservice.controller;

import com.tfg.commissionservice.model.Commission;
import com.tfg.commissionservice.service.CommissionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/commissions")
public class CommissionController {

    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @GetMapping("/{id}")
    public Commission getCommission(@PathVariable Long id) {
        return commissionService.getCommission(id);
    }

    @GetMapping
    public List<Commission> getAllCommissions() {
        return commissionService.getAllCommissions();
    }
}
