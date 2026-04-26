package com.tfg.productionservice.controller;

import com.tfg.productionservice.model.Production;
import com.tfg.productionservice.service.ProductionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productions")
public class ProductionController {

    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @PostMapping
    public Production createProduction(@RequestParam int amount) {
        return productionService.createProduction(amount);
    }

    @GetMapping("/{id}")
    public Production getProduction(@PathVariable Long id) {

        return productionService.getProduction(id);
    }

    @GetMapping
    public List<Production> getAllProductions() {

        return productionService.getAllProductions();
    }
}
