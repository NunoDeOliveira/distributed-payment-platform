package com.tfg.ledgerservice.controller;

import com.tfg.ledgerservice.model.Movement;
import com.tfg.ledgerservice.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/deliveries")
public class LedgerController {
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }


    @GetMapping("/{id}")
    public Movement getLedgerMovement(@PathVariable Long id) {
        return ledgerService.getLedgerMovement(id);
    }

    @GetMapping
    public List<Movement> getAllMovements() {
        return ledgerService.getAllMovements();
    }

}
