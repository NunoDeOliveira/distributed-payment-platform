package com.tfg.accountservice.service;

import com.tfg.accountservice.message.AccountPublish;
import com.tfg.accountservice.model.*;
import com.tfg.accountservice.repository.BalanceRepository;
import com.tfg.accountservice.repository.AccountBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;


@Service
public class AccountService {
    private final AccountPublish accountPublish;
    private final BalanceRepository balanceRepo;
    private static final Long ACCOUNT_ID = 1L;
    private final AccountBalanceRepository accountBalanceRepo;

    // Constructor
    public AccountService(AccountPublish accountPublish, BalanceRepository balanceRepo, AccountBalanceRepository accountBalanceRepo) {
        this.accountPublish = accountPublish;
        this.balanceRepo = balanceRepo;
        this.accountBalanceRepo = accountBalanceRepo;
    }
    

    //@Transactional
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void reserveAmount(String correlationId, BigDecimal amount) {
        // Check input data
        if (correlationId == null || amount == null) {
            return;
        }

        Balance current = balanceRepo.findTopByOrderByIdDesc()
                              .orElseThrow(() -> new RuntimeException("No balance found"));

        if (current.getBalanceAccount().compareTo(amount) < 0) {
            accountPublish.publishAmountRejected(correlationId);
            return;
        }

        BigDecimal newBalance = current.getBalanceAccount().subtract(amount);

        //Update the state and publish
        Balance balance = new Balance(correlationId, newBalance, BalanceState.RESERVED);
        balance.reserved();
        balanceRepo.save(balance);
        accountPublish.publishAmountReserved(correlationId, amount);
    }

    //@Transactional
    @Transactional
    public void deductAmount(String correlationId, BigDecimal amount) {
        if (correlationId == null) {
            return;
        }

        // if it was already released, do not release it again
        Balance balance = balanceRepo.findByCorrelationId(correlationId).orElse(null);
        if (balance == null || balance.getState() != BalanceState.RESERVED) {
            return;
        }

        // // Save the state of balance as debited
        balance.confirm();
        balanceRepo.save(balance);
        // publish the event into queue
        accountPublish.publishAmountDeducted(correlationId, amount);

    }

    // Given and id of product and amount release a reservation
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void cancelReserveAmount(String correlationId, BigDecimal amount) {
        if (correlationId == null || amount == null) return;

        Balance balance = balanceRepo.findByCorrelationId(correlationId).orElse(null);
        if (balance == null || balance.getState() != BalanceState.RESERVED) return;

        BigDecimal restoredBalance = balance.getBalanceAccount().add(amount);
        balance.setBalanceAccount(restoredBalance);
        balance.canceled();
        balanceRepo.save(balance);

        accountPublish.publishAmountCanceled(correlationId, amount);
    }

    // Get the current balance stored in the account
    private BigDecimal getCurrentBalance() {
    
            return accountBalanceRepo.findById(ACCOUNT_ID).map(AccountBalance::getCurrentBalance)
                    .orElseThrow(() -> new IllegalStateException("Account balance not initialized"));
    }

    // Given an amount deposit that amount into the account
    @Transactional//(isolation = Isolation.SERIALIZABLE)
    public String addBalance(BigDecimal balanceAdded) {
        // Check the input data
        if (balanceAdded == null) {
            throw new IllegalArgumentException("Deposit cannot be null");
        }
        if (balanceAdded.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit must be greater than zero");
        }
        
        /****** block account balances with id=1 *******/
        AccountBalance accountBalance = accountBalanceRepo.findByIdForUpdate(ACCOUNT_ID)
                                        .orElseThrow(() -> 
                                        new IllegalStateException("Account balance not initialized"));
        

        String correlationId = UUID.randomUUID().toString();
        // Get currentBalance and add balanceAdded to balance
        BigDecimal currentBalance = getCurrentBalance();
        BigDecimal newBalance = currentBalance.add(balanceAdded);

        // Create a instance of balance 
        Balance balance = new Balance();
        
        balance.setAmount(balanceAdded);
        //balance.setBalanceAccount(balanceAdded.abs());
        balance.setBalanceAccount(newBalance);
        balance.setCorrelationId(correlationId);
        balance.deposit();
        
        // Save the the balance to the respository
        balanceRepo.save(balance);
        
        // Publish to Ledger Service queue to register the movement
        accountPublish.publishDepositCreated(correlationId, balanceAdded);
        
        return correlationId;
    }

    // Get available balance
    public BigDecimal getAvailableBalance() {
        BigDecimal availableBalance = BigDecimal.ZERO;

        List<Balance> balances = balanceRepo.findAll();

        for (Balance balance : balances) {

            BalanceState state = balance.getState();
            BigDecimal amount = balance.getBalanceAccount();
            if (amount == null) {
                continue;
            }

            if (state == null || state == BalanceState.RESERVED || state == BalanceState.CONFIRMED) {
                availableBalance = availableBalance.add(amount);
            }
        }

        return availableBalance;
    }



}
