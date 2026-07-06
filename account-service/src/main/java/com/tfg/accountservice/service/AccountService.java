package com.tfg.accountservice.service;

import com.tfg.accountservice.message.AccountPublish;
import com.tfg.accountservice.model.*;
import com.tfg.accountservice.repository.BalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;


@Service
public class AccountService {
    private final AccountPublish accountPublish;
    private final BalanceRepository balanceRepo;

    // Constructor
    public AccountService(AccountPublish accountPublish, BalanceRepository balanceRepo) {
        this.accountPublish = accountPublish;
        this.balanceRepo = balanceRepo;
    }
    

    //@Transactional
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void reserveAmount(String correlationId, BigDecimal amount) {
        // Check input data
        if (correlationId == null || amount == null) {
            return;
        }

        // Check if there are Balance records with the given ID
        Optional<Balance> amountToReserve = balanceRepo.findByCorrelationId(correlationId);
        if (amountToReserve.isPresent()) {
            return;
        }

        // Calculate  available balance from repository
        BigDecimal available = balanceRepo.calculateAvailableBalance(
                List.of(BalanceState.RESERVED, BalanceState.CONFIRMED));

        // Check if there are enough funds
        if (available.compareTo(amount) < 0) {
            accountPublish.publishAmountRejected(correlationId);
            return;
        }

        // Save the state of amount reserved. -->String correlationId, BigDecimal amount, BalanceState state
        Balance amountReserved = new Balance(correlationId,amount, BalanceState.RESERVED);
        balanceRepo.save(amountReserved);

        // Publish into Ledger queue
        accountPublish.publishAmountReserved(correlationId, amount);
    }

    //@Transactional
    //public void validateDelivery(Long id, int amount){
    @Transactional
    public void deductAmount(String correlationId) {
        if (correlationId == null) {
            return;
        }

        Balance balance = balanceRepo.findByCorrelationId(correlationId).orElse(null);

        if (balance == null) {
            return;
        }

        if (balance.getState() == BalanceState.CONFIRMED) {
            return;
        }

        if (balance.getState() != BalanceState.RESERVED) {
            return;
        }

        // // Save the state of balance as debited
        balance.debited();
        balanceRepo.save(balance);

        // Release the amount reserved
        Balance balanceSaved = balanceRepo.findByCorrelationId(correlationId).orElse(null);
        releaseAmount(correlationId);
    }

    @Transactional
    // Given an id and amount of a Production increase the stock of DB
    public void releaseAmount(String correlationId) {
        if (correlationId == null) {
            return;
        }

        // Idempotency: if it was already released, do not release it again
        Balance balance = balanceRepo.findByCorrelationId(correlationId).orElse(null);
        if (balance.getState() == BalanceState.RELEASED) {
            return;
        }

        // Only reserved amounts can be released
        if (balance.getState() != BalanceState.RESERVED) {
            return;
        }

        // Change local state to RELEASED
        balance.released();
        balanceRepo.save(balance);

        // Publish compensation event
        accountPublish.publishAmountReleased(correlationId);
    }

    // Given and id of product and amount release a reservation
    @Transactional
    public void cancelReserveAmount(String correlationId) {
        // Check input data
        if (correlationId == null) {
            return;
        }

        // Idempotency: if it is already released, do not release it again
        Balance balance = balanceRepo.findByCorrelationId(correlationId).orElse(null);
        if (balance.getState() == BalanceState.RELEASED) {
            return;
        }

        // Only a reserved amount can be cancelled and released
        if (balance.getState() != BalanceState.RESERVED) {
            return;
        }

        // Local trace: cancelled is not published as a Saga event
        balance.canceled();
        balanceRepo.save(balance);
        // Final state for the Saga compensation
        balance.released();
        balanceRepo.save(balance);

        accountPublish.publishAmountReleased(correlationId);
    }



    // Get available balance
    public BigDecimal getAvailableBalance() {

        BigDecimal availableBalance = BigDecimal.ZERO;

        List<Balance> balances = balanceRepo.findAll();

        for (Balance balance : balances) {

            BalanceState state = balance.getState();
            BigDecimal amount = balance.getAmount();
            if (amount == null) {
                continue;
            }

            if (state == null || state == BalanceState.RESERVED || state == BalanceState.CONFIRMED) {
                availableBalance = availableBalance.add(amount);
            }
        }

        return availableBalance;
    }

    // Given an amount deposit that amount into the account
    @Transactional
    public void addBalance(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Balance balance = new Balance();
        balance.setAmount(amount);
        balance.setTime(LocalDateTime.now());

        balanceRepo.save(balance);
    }


}
