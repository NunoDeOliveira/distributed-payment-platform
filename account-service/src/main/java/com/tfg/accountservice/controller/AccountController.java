package com.tfg.accountservice.controller;

import com.tfg.accountservice.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/balances")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Insert balance
    @PostMapping("/add")
    public Map<String, String> addBalance(@RequestParam BigDecimal balanceAccount) {

        accountService.addBalance(balanceAccount);

        return Map.of("status", "successful");
    }

}
