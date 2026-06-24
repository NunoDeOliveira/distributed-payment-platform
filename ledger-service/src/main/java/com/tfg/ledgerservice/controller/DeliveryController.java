package com.tfg.ledgerservice.controller;

import com.tfg.ledgerservice.model.Delivery;
import com.tfg.ledgerservice.service.DeliveryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {
    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public Delivery reserveDelivery(@RequestParam Long productionId,
                                    @RequestParam int amount) {
        return deliveryService.reserveDelivery(productionId, amount);
    }

    @GetMapping("/{id}")
    public Delivery getDelivery(@PathVariable Long id) {
        return deliveryService.getDelivery(id);
    }

    @GetMapping
    public List<Delivery> getAllProductions() {
        return deliveryService.getAllDeliveries();
    }
    
    @DeleteMapping("/{id}")
    public void cancelDelivery(@PathVariable Long id) {
        deliveryService.cancelDelivery(id);
    }
   
   
}
