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

    /*@Transactional
    public void releaseAmount(String correlationId, BigDecimal amount) {
        if (correlationId == null || amount == null) {
            return;
        }

        // If it was already released, do not release it again
        Balance balance = balanceRepo.findByCorrelationId(correlationId).orElse(null);
        if (balance == null ||
                balance.getState() == BalanceState.RELEASED ||
                balance.getState() != BalanceState.RESERVED) {
            return;
        }

        // Get balance of correlationId given
        BigDecimal reservedAmount = balance.getAmount();

        // Sum the amount release to account balance
        BigDecimal currentBalance = getCurrentBalance();
        BigDecimal newBalance = currentBalance.add(reservedAmount);

        // Change local state to RELEASED and sum amount
        balance.setBalanceAccount(newBalance);
        balance.setState(BalanceState.RELEASED);
        balance.released();
        balanceRepo.save(balance);

        // Update cancel state before to release
        cancelReserveAmount(correlationId, amount);

    }*/

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
        Optional<Balance> lastBalance = balanceRepo.findTopByOrderByIdDesc();
        if (lastBalance.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return lastBalance.get().getBalanceAccount();

    }

    // Given an amount deposit that amount into the account
    @Transactional
    public void addBalance(BigDecimal balanceAdded) {
        // Check the input data
        if (balanceAdded == null || balanceAdded.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Get currentBalance and add balanceAdded to balance
        BigDecimal currentBalance = getCurrentBalance();
        BigDecimal newBalance = currentBalance.add(balanceAdded);

        //
        Balance balance = new Balance();
        balance.setBalanceAccount(balanceAdded.abs());
        balance.setBalanceAccount(newBalance);
        balance.setTime(LocalDateTime.now());

        balanceRepo.save(balance);
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
