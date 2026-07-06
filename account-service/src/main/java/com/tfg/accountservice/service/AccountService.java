package com.tfg.accountservice.service;

import com.tfg.accountservice.message.AccountPublish;
import com.tfg.accountservice.model.Account;
import com.tfg.accountservice.model.Operation;
import com.tfg.accountservice.model.OperationState;
import com.tfg.accountservice.repository.AccountRepository;
import com.tfg.accountservice.repository.OperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.transaction.annotation.Isolation;


@Service
public class AccountService {
    private final AccountRepository accountRepo;
    private final AccountPublish accountPublish;
    private final OperationRepository operationRepo;

    // Constructor
    public AccountService(AccountRepository accountRepo, AccountPublish accountPublish,
                          OperationRepository operationRepo) {
        this.accountRepo = accountRepo;
        this.accountPublish = accountPublish;
        this.operationRepo = operationRepo;
    }
    

    //@Transactional
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void reserveAmount(String correlationId, BigDecimal amountToDeducted) {
        // Check input data
        if (correlationId == null || amountToDeducted == null) {
            return;
        }

        // Idempotency. Check if the operation already exists
        if (operationRepo.findByCorrelationId(correlationId).isPresent()) {
            return;
        }

        // Search operation by accountId
        Account account = accountRepo.findByCorrelationId(correlationId).orElse(null);
        if (account == null) {
            // Case operation does not exist
            accountPublish.publishOperationRejected(correlationId);
            return;
        }

        // create operation record
        Operation operation = new Operation(correlationId, amountToDeducted, OperationState.HELD,
                                                                    null, null);

        // Check if the balance is enough
        OperationState result = checkBalance(operation, amountToDeducted, operation);
        if (result == OperationState.REJECTED) {
            accountPublish.publishOperationRejected(correlationId);
            return;
        }

        // Publish event
        accountPublish.publishHoldFunds(correlationId, amountToDeducted);
    }

    //@Transactional
    //public void validateDelivery(Long id, int amount){
    @Transactional
    public void deductAccount(String correlationId) {
        if (correlationId == null) {
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
        accountPublish.publishAccountDeducted(correlationId, amountToDeducted);
    }

    @Transactional
    // Given an id and amount of a Production increase the stock of DB
    public void releaseOperation(String correlationId) {
        if (correlationId == null) {
            return;
        }


        /* ******************* NEW CODE ******************* */
        BigDecimal newAvailableBalance =
                balance.getAvailableBalance().add(balance.getAmount());

        balance.setAvailableBalance(newAvailableBalance);
        balance.released();

        balanceRepository.save(balance);
        accountPublish.publishAmountReleased(correlationId);


        /* ******************* ENd ******************* */





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
        Operation account = accountRepo.findByCorrelationId(correlationId).orElse(null);
        if (account == null) {
            return;
        }
        account.release(operation.getAmount());
        accountRepo.save(account);

        // Mark the operation as released
        operation.released();
        operationRepo.save(operation);

        // Notify Commission Service so it can cancel the commission
        accountPublish.publishOperationReleased(correlationId);
    }

    // Given and id of product and amount release a reservation
    @Transactional
    public void cancelOperation(String correlationId) {
        // Check input data
        if (correlationId == null) {
            return;
        }

        Operation operation = operationRepo.findByCorrelationId(correlationId).orElse(null);
        if (operation == null) return;

        if (operation.getState() != OperationState.HELD) return;

        operation.canceled();
        operationRepo.save(operation);
    }












    // Given an id and amount check the balance
    public OperationState checkBalance(Operation account, BigDecimal amountToDeducted,
                                       Operation operation) {
        if (operation == null || amountToDeducted == null) {
            return OperationState.FAILED;
        }


        /* ******************* NEW CODE ******************* */
        if (balance.getAvailableBalance().compareTo(balance.getAmount()) < 0) {
            balance.rejected();
            balanceRepository.save(balance);
            accountPublish.publishAmountRejected(correlationId);
            return;
        }

        balance.reserved();
        balanceRepository.save(balance);
        accountPublish.publishAmountReserved(correlationId, amount);

        /* ******************* END NEW CODE ******************* */


        boolean enoughBalance = operation.getBalance().compareTo(amountToDeducted) >= 0;
        if (!enoughBalance) {
            operation.rejected();
            operationRepo.save(operation);
            return OperationState.REJECTED;
        }

        // Case there are enough balance
        operation.held();
        operationRepo.save(operation);
        return OperationState.HELD;
    }

    // Given an id of get account
    public Operation getAccount(Long id) {
        if (id == null) return null;
        return operationRepo.findById(id).orElse(null);
    }

    // Get all the operations
    public List<Operation> getAllAccounts() {
        return operationRepo.findAll();
    }


}
