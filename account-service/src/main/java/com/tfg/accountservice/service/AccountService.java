package com.tfg.accountservice.service;

import com.tfg.accountservice.message.AccountPublish;
import com.tfg.accountservice.model.Account;
import com.tfg.accountservice.model.OperationState;
import com.tfg.accountservice.repository.AccountRepository;
import com.tfg.accountservice.repository.OperationRepository;
import org.springframework.stereotype.Service;
import com.tfg.accountservice.model.Operation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Isolation;


@Service
public class AccountService {
    private final AccountRepository accountRepo;
    private final AccountPublish accountPublish;
    private final OperationRepository operationRepo;

    // Constructor
    public AccountService(AccountRepository accountRepo, AccountPublish accountPublish, OperationRepository operationRepo) {
        this.accountRepo = accountRepo;
        this.accountPublish = accountPublish;
        this.operationRepo = operationRepo;
    }
    

    //@Transactional
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void holdFunds(Long accountId, String correlationId, BigDecimal amountToDeducted) {
        // Idempotency. Check if the operation already exists
        if (operationRepo.findByCorrelationId(correlationId).isPresent()) {
            return;
        }

        // Search account by accountId
        Account account = accountRepo.findById(accountId).orElse(null);
        if (account == null) {
            // Case account does not exist
            accountPublish.publishOperationRejected(accountId, correlationId);
            return;
        }

        // create operation record
        Operation operation = new Operation(accountId, correlationId, amountToDeducted,
                                            OperationState.HELD, LocalDateTime.now(), LocalDateTime.now());

        // Check if the balance is enough
        OperationState result = checkBalance(account, amountToDeducted, operation);
        if (result == OperationState.REJECTED) {
            accountPublish.publishOperationRejected(accountId, correlationId);
            return;
        }

        // Publish event
        accountPublish.publishHoldFunds(accountId, correlationId, amountToDeducted);
    }

    //@Transactional
    //public void validateDelivery(Long id, int amount){
    @Transactional
    public void deductAccount(Long accountId, String correlationId) {
        if (correlationId == null || accountId == null) {
            return;
        }

        // Recover the existing operation
        Operation operation = operationRepo.findByCorrelationId(correlationId).orElse(null);
        if (operation == null) {
            return;
        }

        // Extract amount from the operation — not a parameter here
        BigDecimal amountToDeducted = operation.getAmount();

        // Confirm the hold is now a permanent debit
        operation.deducted();
        operationRepo.save(operation);

        // Notify in queue that the full chain succeeded
        accountPublish.publishAccountDeducted(accountId, correlationId, amountToDeducted);
    }

    @Transactional
    // Given an id and amount of a Production increase the stock of DB
    public void releaseOperation(Long paymentId, String correlationId, Long accountId) {
        if (paymentId == null || correlationId == null || accountId == null) {
            return;
        }

        // Recover the existing hold — if not found, nothing to release
        Operation operation = operationRepo.findByCorrelationId(correlationId).orElse(null);
        if (operation == null) {
            return;
        }

        // Only release if the operation is still held
        if (operation.getState() != OperationState.HELD) {
            return; // idempotency guard — already released or deducted
        }

        // Restore the balance in the account
        Account account = accountRepo.findById(accountId).orElse(null);
        if (account == null) {
            return;
        }
        account.release(operation.getAmount());
        accountRepo.save(account);

        // Mark the operation as released
        operation.released();
        operationRepo.save(operation);

        // Notify Commission Service so it can cancel the commission
        accountPublish.publishOperationReleased(paymentId, correlationId);
    }

    // Given and id of product and amount release a reservation
    @Transactional
    public void cancelOperation(Long accountId, String correlationId) {
        // Check input data
        if (accountId == null || correlationId == null) {
            return;
        }
    }

    // Given an id and amount check the balance
    public OperationState checkBalance(Account account, BigDecimal amountToDeducted, Operation operation) {
        if (account == null || amountToDeducted == null) {
            return OperationState.FAILED;
        }

        // Compare the balance with amount to deducted (amountToDeducted)
        boolean enoughBalance = account.getBalance().compareTo(amountToDeducted) >= 0;

        // Check if the balance is enough
        if (!enoughBalance) {
            // Case there are not enough balance
            operation.rejected();
            operationRepo.save(operation);
            return OperationState.REJECTED;
        }

        // Case there are enough balance
        account.held(amountToDeducted);
        accountRepo.save(account);
        operation.held();
        operationRepo.save(operation);
        return OperationState.HELD;
    }

    // Given an id of get acount
    public Account getAccount(Long accountId) {
        // Check input data
        if (accountId == null) {
            System.out.println("Incorrect input data");
            return null;
        }

        return accountRepo.findById(accountId).orElse(null);
    }

    // Get all the operations
    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }


}
