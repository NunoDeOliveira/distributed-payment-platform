package com.tfg.transferservice.service;

import com.tfg.transferservice.message.TransferPublish;
import com.tfg.transferservice.model.Transfer;
import com.tfg.transferservice.model.TransferState;
import com.tfg.transferservice.repository.TransferRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
//import org.springframework.scheduling.annotation.Scheduled;


@Service
public class TransferService {

    private final TransferRepository productionRepository;
    private final TransferPublish productionPublish;

    public TransferService(TransferRepository productionRepository,
                             TransferPublish productionPublish) {
        this.productionRepository = productionRepository;
        this.productionPublish = productionPublish;
    }

    // Given an ID and amount of Products create a Transfer
    public Transfer createTransfer(int amount) {
        Transfer newTransfer = new Transfer(
                    amount, TransferState.CREATED, LocalDateTime.now());

        Transfer storedTransfer = productionRepository.save(newTransfer);

        // Publish on RabbitMQ for consume this event
        productionPublish.publishTransferCreated(
                        storedTransfer.getId(), storedTransfer.getAmount());
                        
        // Change production state to WAITING while waiting
        waitingResponse(storedTransfer.getId());

        return storedTransfer;
    }
    
    // Change production state from CREATED to WAITING
    public void waitingResponse(Long productionId) {
        Transfer production = productionRepository.findById(productionId).orElse(null);        
        if (production == null || production.getState() != TransferState.CREATED) {
            return;
        }
        
        production.waiting();
        productionRepository.save(production);
    }
   
    @Async
    // Given an ID of production from the RabbitMQ start a new production
    public void startTransfer(Long productionId) {
        Transfer production = productionRepository.findById(productionId).orElse(null);
        if (production == null || production.getState() != TransferState.WAITING) {
            return;
        }
        
        production.start();
        // Update state in database
        productionRepository.save(production);

        try{
            // Simulate the production processing (0,001 seconds)
            Thread.sleep(500);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return;
        }
        // Apply the logic for a completed production when de production finish
        completeTransfer(productionId);
    }

    // Method for saving a rejected production in the DB
    public void rejectTransfer(Transfer productionRejected, int amountAllowed) {   
        int originalAmount = productionRejected.getAmount();
        // Case the stock is completed
        if (amountAllowed == 0) {
            productionRejected.reject();
            productionRepository.save(productionRejected);
            
        // Case needed compensation transaction. Stock is not completed    
        } else if (amountAllowed > 0 && amountAllowed < originalAmount) {
            productionRejected.reject(); 
            productionRepository.save(productionRejected);
            compensateRejectedTransfer(productionRejected, amountAllowed);
            
         // This case should not happen   
        } else {
            System.out.println("WARNING: There is an error with productionId=" 
                                                + productionRejected.getId());
        }
    }
    
    // Saga compensating transaction method.
    // Given a rejected production and the maximum amount allowed for that production
    public void compensateRejectedTransfer(Transfer productionRejected, 
                                                    int maxAllowedAmount) {
        int originalAmount = productionRejected.getAmount();
        int pendingAmount = originalAmount - maxAllowedAmount;
      
        // Create new protuction with allowd amount
        createTransfer(maxAllowedAmount);
        
        System.out.println("Partial compensation: created new production with amount=" 
                            + maxAllowedAmount);
        // Save the rest of the production rejected as PENDING
        /*if (pendingAmount > 0) {
            Transfer newPending = new Transfer(
                        pendingAmount, TransferState.PENDING, LocalDateTime.now());
            productionRepository.save(newPending);
        }*/
    }
    
    @Transactional
    // When the production is completed save production in repository
    // and publish an event on RabbitMQ
    public void completeTransfer(Long productionId) {
        Transfer production = productionRepository.findById(productionId).orElse(null);    
        if (production == null || production.getState() == TransferState.CANCELLED ||
                                  production.getState() == TransferState.COMPLETED) {
            return;
        }
        
        production.complete();
        productionRepository.save(production);
        // Method to send event to RabbitMQ
        publishTransferCompleted(production.getId(), production.getAmount());
    }
    
    // Given an Id of a production publish that production in the queue
    private void publishTransferCompleted(Long productionId, int amount) {
        Transfer production = productionRepository.findById(productionId).orElse(null);
        if (production == null) {
            return;
        }
        
        // send event to RabbitMQ
        productionPublish.publishTransferCompleted(
                                    production.getId(), production.getAmount());
    }
    
    // Get production by ID
    public Transfer getTransfer(Long id) {
        Optional<Transfer> production = productionRepository.findById(id);
        Transfer productionToReturn = production.orElseThrow(()
                    -> new RuntimeException("Transfer " + id + "not found"));

        return productionToReturn;
    }
    
    // Given an id of production cancell that production
    public void cancelTransfer(Long id) {
        Transfer production = productionRepository.findById(id).orElse(null);
        if (production == null || 
            production.getState() == TransferState.COMPLETED ||
            production.getState() == TransferState.CANCELLED) {  
            return;
        } 
        production.cancelled();
        productionRepository.save(production);
    }
    
    // Given an id of production cancell that production
    public void cancelTransferByUser(Long id) {
        Transfer production = productionRepository.findById(id).orElse(null);
        if (production == null || 
            production.getState() == TransferState.COMPLETED ||
            production.getState() == TransferState.CANCELLED) {  
            return;
        } 
        production.cancelled();
        productionRepository.save(production);
        productionPublish.publishTransferCancelled(id, production.getAmount());
    }
    
    
    

    // Get all the production from the repository
    // This query is to return to the user
    public List<Transfer> getAllTransfers() {
        return productionRepository.findAll();
    }
    
        /*
    // Given an rejeted production manage timeout and fail 
    private void handleRetry(Transfer productionRejected) {
        productionRejected.incrementRetry();
        
        // Case faill 3 times the state will be failed
        if (productionRejected.getRetryCount() >= 3) {
            productionRejected.fail();
            productionRepository.save(productionRejected);
            
        // case fail 1 time the state will be pending
        } else {
            productionRejected.pending();
            productionRepository.save(productionRejected);
        }
    }
    
      
    // When a production is rejected because it excceds the stock 
    // and is assigned as PENDING, this method start this a pending production
    //@Scheduled(fixedDelay = 10000)
    public void processPendingTransfers() {
        Optional<Transfer> pending = productionRepository
                  .findFirstByStateOrderByStartTimeAsc(TransferState.PENDING);
        if (pending.isPresent()) {
            productionPublish.publishTransferPending(
                              pending.get().getId(), pending.get().getAmount());
        }
    }*/
    
    // If inventory connection fail get timeout state
    public void getTimeoutState(Long productionId) {
        Transfer production = productionRepository.findById(productionId).orElse(null);        
        if (production == null) {
            return;
        }
        production.timeout();
        productionRepository.save(production);
    }
    
    /*
    // When the third retry fails, the state is failed
    public void getFailedSate(Transfer production) {
        production.fail();
        productionRepository.save(production);
    }*/
    
}
