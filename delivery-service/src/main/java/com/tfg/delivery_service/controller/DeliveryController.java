package com.tfg.delivery_service.controller;

import com.tfg.delivery_service.model.Delivery;
import com.tfg.delivery_service.service.DeliveryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productions")
public class DeliveryController {
    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public Delivery createProduction(@RequestParam int amount) {

        return deliveryService.createDelivery(amount);
    }

    @GetMapping("/{id}")
    public Delivery getDelivery(@PathVariable Long id) {

        return deliveryService.getDelivery(id);
    }

    @GetMapping
    public List<Delivery> getAllProductions() {

        return deliveryService.getAllDeliveries();
    }

}
