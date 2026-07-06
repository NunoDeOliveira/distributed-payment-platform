package com.tfg.accountservice.controller;

import com.tfg.accountservice.model.Account;
import com.tfg.accountservice.model.Operation;
import com.tfg.accountservice.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operations")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService paymentService) {
        this.accountService = paymentService;
    }


    @GetMapping("/{id}")
    public Operation getPayment(@PathVariable Long id) {

        return accountService.getAccount(id);
    }

    @GetMapping
    public List<Account> getAllPayments() {

        return accountService.getAllAccounts();
    }


}
