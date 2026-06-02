package com.tfg.productionservice.service;

import com.tfg.productionservice.message.ProductionPublish;
import com.tfg.productionservice.model.Production;
import com.tfg.productionservice.model.ProductionState;
import com.tfg.productionservice.repository.ProductionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
//import org.springframework.scheduling.annotation.Scheduled;


@Service
public class ProductionService {

    private final ProductionRepository productionRepository;
    private final ProductionPublish productionPublish;

    public ProductionService(ProductionRepository productionRepository,
                             ProductionPublish productionPublish) {
        this.productionRepository = productionRepository;
        this.productionPublish = productionPublish;
    }

    // Given an ID and amount of Products create a Production
    public Production createProduction(int amount) {
        Production newProduction = new Production(
                    amount, ProductionState.CREATED, LocalDateTime.now());

        Production storedProduction = productionRepository.save(newProduction);

        // Publish on RabbitMQ for consume this event
        productionPublish.publishProductionCreated(
                        storedProduction.getId(), storedProduction.getAmount());
                        
        // Change production state to WAITING while waiting
        waitingResponse(storedProduction.getId());

        return storedProduction;
    }
    
    // Change production state from CREATED to WAITING
    public void waitingResponse(Long productionId) {
        Production production = productionRepository.findById(productionId).orElse(null);        
        if (production == null || production.getState() != ProductionState.CREATED) {
            return;
        }
        
        production.waiting();
        productionRepository.save(production);
    }
   
    @Async
    // Given an ID of production from the RabbitMQ start a new production
    public void startProduction(Long productionId) {
        Production production = productionRepository.findById(productionId).orElse(null);
        if (production == null || production.getState() != ProductionState.WAITING) {
            return;
        }
        
        production.start();
        // Update state in database
        productionRepository.save(production);

        try{
            // Simulate the production processing (0,001 seconds)
            Thread.sleep(300);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return;
        }
        // Apply the logic for a completed production when de production finish
        completeProduction(productionId);
    }

    // Method for saving a rejected production in the DB
    public void rejectProduction(Production productionRejected, int amountAllowed) {   
        int originalAmount = productionRejected.getAmount();
        // Case the stock is completed
        if (amountAllowed == 0) {
            productionRejected.reject();
            productionRepository.save(productionRejected);
            
        // Case needed compensation transaction. Stock is not completed    
        } else if (amountAllowed > 0 && amountAllowed < originalAmount) {
            productionRejected.reject(); 
            productionRepository.save(productionRejected);
            compensateRejectedProduction(productionRejected, amountAllowed);
            
         // This case should not happen   
        } else {
            System.out.println("WARNING: There is an error with productionId=" 
                                                + productionRejected.getId());
        }
    }
    
    // Saga compensating transaction method.
    // Given a rejected production and the maximum amount allowed for that production
    public void compensateRejectedProduction(Production productionRejected, 
                                                    int maxAllowedAmount) {
        int originalAmount = productionRejected.getAmount();
        int pendingAmount = originalAmount - maxAllowedAmount;
      
        // Create new protuction with allowd amount
        createProduction(maxAllowedAmount);
        
        System.out.println("Partial compensation: created new production with amount=" 
                            + maxAllowedAmount);
        // Save the rest of the production rejected as PENDING
        /*if (pendingAmount > 0) {
            Production newPending = new Production(
                        pendingAmount, ProductionState.PENDING, LocalDateTime.now());
            productionRepository.save(newPending);
        }*/
    }
    
    @Transactional
    // When the production is completed save production in repository
    // and publish an event on RabbitMQ
    public void completeProduction(Long productionId) {
        Production production = productionRepository.findById(productionId).orElse(null);    
        if (production == null || production.getState() == ProductionState.CANCELLED ||
                                  production.getState() == ProductionState.COMPLETED) {
            return;
        }
        
        production.complete();
        productionRepository.save(production);
        // Method to send event to RabbitMQ
        publishProductionCompleted(production.getId(), production.getAmount());
    }
    
    // Given an Id of a production publish that production in the queue
    private void publishProductionCompleted(Long productionId, int amount) {
        Production production = productionRepository.findById(productionId).orElse(null);
        if (production == null) {
            return;
        }
        
        // send event to RabbitMQ
        productionPublish.publishProductionCompleted(
                                    production.getId(), production.getAmount());
    }
    
    // Get production by ID
    public Production getProduction(Long id) {
        Optional<Production> production = productionRepository.findById(id);
        Production productionToReturn = production.orElseThrow(()
                    -> new RuntimeException("Production " + id + "not found"));

        return productionToReturn;
    }
    
    // Given an id of production cancell that production
    public void cancelProduction(Long id) {
        Production production = productionRepository.findById(id).orElse(null);
        if (production == null || 
            production.getState() == ProductionState.COMPLETED ||
            production.getState() == ProductionState.CANCELLED) {  
            return;
        } 
        production.cancelled();
        productionRepository.save(production);
    }
    
    // Given an id of production cancell that production
    public void cancelProductionByUser(Long id) {
        Production production = productionRepository.findById(id).orElse(null);
        if (production == null || 
            production.getState() == ProductionState.COMPLETED ||
            production.getState() == ProductionState.CANCELLED) {  
            return;
        } 
        production.cancelled();
        productionRepository.save(production);
        productionPublish.publishProductionCancelled(id, production.getAmount());
    }
    
    
    

    // Get all the production from the repository
    // This query is to return to the user
    public List<Production> getAllProductions() {
        return productionRepository.findAll();
    }
    
        /*
    // Given an rejeted production manage timeout and fail 
    private void handleRetry(Production productionRejected) {
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
    public void processPendingProductions() {
        Optional<Production> pending = productionRepository
                  .findFirstByStateOrderByStartTimeAsc(ProductionState.PENDING);
        if (pending.isPresent()) {
            productionPublish.publishProductionPending(
                              pending.get().getId(), pending.get().getAmount());
        }
    }*/
    
    // If inventory connection fail get timeout state
    public void getTimeoutState(Long productionId) {
        Production production = productionRepository.findById(productionId).orElse(null);        
        if (production == null) {
            return;
        }
        production.timeout();
        productionRepository.save(production);
    }
    
    /*
    // When the third retry fails, the state is failed
    public void getFailedSate(Production production) {
        production.fail();
        productionRepository.save(production);
    }*/
    
}
