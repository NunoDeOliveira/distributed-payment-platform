package com.tfg.productionservice.service;

import com.tfg.productionservice.message.ProductionPublish;
import com.tfg.productionservice.model.Production;
import com.tfg.productionservice.model.ProductionState;
import com.tfg.productionservice.repository.ProductionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


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
                    amount, ProductionState.PENDING, LocalDateTime.now());

        Production storedProduction = productionRepository.save(newProduction);

        // Publish on RabbitMQ for consume this event
        productionPublish.publishProductionCreated(
                        storedProduction.getId(), storedProduction.getAmount());

        return storedProduction;
    }

    // Given an ID of production from the RabbitMQ start a new production
    @Async
    public void startProduction(Production production) {
        production.start();
        // Update state in database
        productionRepository.save(production);

        try{
            // Simulate the production processing (30 seconds)
            Thread.sleep(3000);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            System.out.println(e);
        }
        // Apply the logic for a completed production when de production finish
        completeProduction(production);
    }

    // Method for saving a rejected production in the DB
    public void rejectProduction(Production production) {
        production.reject();
        productionRepository.save(production);
    }

    // Saga compensating transaction method.
    // Given a rejected production and the maximum amount allowed for that production
    public void compensateRejectedProduction(Production production, int maxAllowedAmount) {
        // Update the production state to reject
        production.reject();
        productionRepository.save(production);
         int originalAmount = production.getAmount();

        // Create a new production limited by the given amount
        if (maxAllowedAmount > 0) {
            createProduction(maxAllowedAmount);
        }

        // Save the rest of the production rejected as PENDING
        int remainingAmount = originalAmount - maxAllowedAmount;
        if (remainingAmount > 0) {
            Production pendingProduction = new Production(
                              remainingAmount, ProductionState.PENDING, LocalDateTime.now());
            productionRepository.save(pendingProduction);
        }
    }

    // When the production is completed save production in repository
    // and publish an event on RabbitMQ
    public void completeProduction(Production production) {
        production.complete();
        productionRepository.save(production);
        // Method to send event to RabbitMQ
        publishProductionCompleted(production);

        // Check if there are pending productions and launch the next one
        Optional<Production> next = productionRepository
                .findFirstByStateOrderByStartTimeAsc(ProductionState.PENDING);

        if (next.isPresent()) {
            Production pending = next.get();
            productionPublish.publishProductionCreated(
                    pending.getId(), pending.getAmount());
        }
    }

    private void publishProductionCompleted(Production production) {
        // send event to RabbitMQ
        productionPublish.publishProductionCompleted(
                                    production.getId(), production.getAmount());
    }
    
    // If inventory connection fail get timeout state
    public void getTimeoutState(Production production) {
        production.timeout();
        productionRepository.save(production);
    }
    
    // When the third retry fails, the state is failed
    public void getFailedSate(Production production) {
        production.fail();
        productionRepository.save(production);
    }
    
    // Get production by ID
    public Production getProduction(Long id) {
        Optional<Production> pro = productionRepository.findById(id);
        Production productionToReturn = pro.orElseThrow(()
                    -> new RuntimeException("Production " + id + "not found"));

        return productionToReturn;
    }
    
    // When a production is rejected because it excceds the stock 
    // and is assigned as PENDING, this method start this a pending production
    public void startNextPending(int availableCapacity) {
        // Find in the respository the production with PENDING state and the lowest ID
        Optional<Production> next = productionRepository
                        .findFirstByStateOrderByStartTimeAsc(ProductionState.PENDING);
        // Check if the Optional containe a value
        if (next.isPresent()) {
            // Get the production found 
            Production production = next.get();
            // Check if the amount of production found is less or equal than stock availability
            if (production.getAmount() <= availableCapacity) {
                productionPublish.publishProductionCreated(
                                  production.getId(), production.getAmount());
            }
        }
    }

    // Get all the production from the repository
    // This query is to return to the user
    public List<Production> getAllProductions() {
        return productionRepository.findAll();
    }
}
