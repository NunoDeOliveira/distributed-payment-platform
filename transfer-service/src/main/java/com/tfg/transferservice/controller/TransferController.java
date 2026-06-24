package com.tfg.transferservice.controller;

import com.tfg.transferservice.model.Transfer;
import com.tfg.transferservice.service.TransferService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productions")
public class TransferController {

    private final TransferService productionService;

    public TransferController(TransferService productionService) {
        this.productionService = productionService;
    }

    @PostMapping
    public Transfer createTransfer(@RequestParam int amount) {
        return productionService.createTransfer(amount);
    }

    @GetMapping("/{id}")
    public Transfer getTransfer(@PathVariable Long id) {
        return productionService.getTransfer(id);
    }

    @GetMapping
    public List<Transfer> getAllTransfers() {
        return productionService.getAllTransfers();
    }
    
    @DeleteMapping("/{id}")
    public void cancelTransferByUser(@PathVariable Long id) {
        productionService.cancelTransferByUser(id);
    }
    
}
