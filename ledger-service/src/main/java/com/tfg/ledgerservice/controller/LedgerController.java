package com.tfg.ledgerservice.controller;

import com.tfg.ledgerservice.model.LedgerMovement;
import com.tfg.ledgerservice.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
public class LedgerController {
    private final LedgerService deliveryService;

    public LedgerController(LedgerService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public LedgerMovement reserveLedgerMovement(@RequestParam Long productionId,
                                    @RequestParam int amount) {
        return deliveryService.reserveLedgerMovement(productionId, amount);
    }

    @GetMapping("/{id}")
    public LedgerMovement getLedgerMovement(@PathVariable Long id) {
        return deliveryService.getLedgerMovement(id);
    }

    @GetMapping
    public List<LedgerMovement> getAllProductions() {
        return deliveryService.getAllDeliveries();
    }
    
    @DeleteMapping("/{id}")
    public void cancelLedgerMovement(@PathVariable Long id) {
        deliveryService.cancelLedgerMovement(id);
    }
   
   
}
