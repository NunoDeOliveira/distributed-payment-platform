package com.tfg.ledgerservice.service;

import com.tfg.ledgerservice.message.LedgerPublish;
import com.tfg.ledgerservice.model.LedgerMovement;
import com.tfg.ledgerservice.model.LedgerMovementState;
import com.tfg.ledgerservice.repository.LedgerMovementRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LedgerService {

    private final LedgerMovementRepository deliveryRepository;
    private final LedgerPublish deliveryPublish;

    public LedgerService(LedgerMovementRepository deliveryRepository, 
                                              LedgerPublish deliveryPublish) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryPublish = deliveryPublish;
    }
    
    // Given an ID and amount of a LedgerMovement create a delivery
    public LedgerMovement reserveLedgerMovement(Long productionId, int amount) {
        if (productionId == null || amount <= 0) {
            return null;
        }
    
        LedgerMovement newLedgerMovement = new LedgerMovement(amount, LedgerMovementState.RESERVED,
                                                    LocalDateTime.now());

        newLedgerMovement.setProductionId(productionId);
        LedgerMovement storedLedgerMovement = deliveryRepository.save(newLedgerMovement);
        deliveryPublish.publishLedgerMovementCreated(storedLedgerMovement.getId(), 
                                                storedLedgerMovement.getAmount());

        return storedLedgerMovement;
    }
    
    @Async
    // Given an ID of delivery from the RabbitMQ, start a new delivery
    public void startLedgerMovement(Long deliveryId) {
        LedgerMovement delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null || delivery.getState() != LedgerMovementState.RESERVED) {
            return;
        }
        
        delivery.start();
        // Update state in database
        deliveryRepository.save(delivery);
        
        try {
            // Wait 0,001 seconds to send delivery completed
            Thread.sleep(500); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Apply the logic to finish delivery
        completeLedgerMovement(deliveryId);
    }
    
    @Transactional
    // When the delivery is completed save delivery in the repository
    // and publish an event on RabbitMQ
    public void completeLedgerMovement(Long deliveryId) {
        LedgerMovement delivery = deliveryRepository.findById(deliveryId).orElse(null);
        // Chech if the delivery received is cancelled
        // the delivery cancelled cannot save as completed delivery 
        if (delivery == null || delivery.getState() != LedgerMovementState.ON_DELIVERY) {
            return;
        }
    
        delivery.complete();
        deliveryRepository.save(delivery);
        // Method to send event to RabbitMQ
        deliveryPublish.publishLedgerMovementCompleted(delivery.getId(), delivery.getAmount());
    }
    
    // Given an amount create a delivery from stock already available in Inventory
    /*public LedgerMovement createLedgerMovementFromStock(int amount) {
        if (amount <= 0) {
            return null;
        }
    
        LedgerMovement newLedgerMovement = new LedgerMovement(amount, LedgerMovementState.CREATED,LocalDateTime.now());
        LedgerMovement storedLedgerMovement = deliveryRepository.save(newLedgerMovement);
        
        // Check if the delivery is saved corretly befor to switch to reserved
        // The create state is only for register
        storedLedgerMovement.reserved();
        deliveryRepository.save(storedLedgerMovement);
        // if the delivery is cancelled publish
        deliveryPublish.publishLedgerMovementCreated(storedLedgerMovement.getId(), storedLedgerMovement.getAmount());

        return storedLedgerMovement;
    }*/
    

    // Get delivery by ID
    public LedgerMovement getLedgerMovement(Long id) {
        Optional<LedgerMovement> deliver = deliveryRepository.findById(id);
        LedgerMovement deliveryToReturn = deliver.orElseThrow(()
                -> new RuntimeException("LedgerMovement " + id + "not found"));

        return deliveryToReturn;
    }
            
    // Get all the deliveries from the repository.
    // This query is to return to the user.
    public List<LedgerMovement> getAllDeliveries() {
        return deliveryRepository.findAll();
    }
    
    // 
    
    /*
    // This method publish start a delivivery
    public void processPendingDeliveries() {
        Optional<LedgerMovement> pending = deliveryRepository
                .findFirstByStateOrderByStartTimeAsc(LedgerMovementState.PENDING);
                
        if (pending.isPresent()) {
            deliveryPublish.publishLedgerMovementPending(
                            pending.get().getId(), pending.get().getAmount());
        }
    }*/
    
    @Transactional
    // Method for saving a cancelled delivery in the DB
    public void cancelLedgerMovement(Long deliveryId) {
        LedgerMovement delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null || delivery.getState() == LedgerMovementState.COMPLETED ||
            delivery.getState() == LedgerMovementState.CANCELLED) {
            return;
        }
        // Save delivery state
        delivery.cancelled();
        deliveryRepository.save(delivery);
        // apply compensate transaction
        compensateCancelledLedgerMovement(deliveryId);
    }

    // Saga compensating transaction method.
    // Given a cancelled delivery release delivery reserved
    public void compensateCancelledLedgerMovement(Long deliveryId) {
        LedgerMovement delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            return;
        }
        
        deliveryPublish.publishReservationRelease(delivery.getId(), delivery.getAmount());
        deliveryPublish.publishLedgerMovementCancelled(
                        delivery.getId(), delivery.getProductionId(), delivery.getAmount());
    }
    
    
    public void cancelLedgerMovementByProductionId(Long productionId) {
        LedgerMovement delivery = deliveryRepository.findByProductionId(productionId).orElse(null);
        if (delivery == null) {
            return;
        }
        cancelLedgerMovement(delivery.getId());
    }
    
    // If inventory connection fail get timeout state
    public void getTimeoutState(Long deliveryId) {
        LedgerMovement delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            return;
        }
        delivery.timeout();
        deliveryRepository.save(delivery);
    }
    

    /*
    // When the third retry fails, the state is failed
    public void getFailedSate(LedgerMovement delivery) {
        delivery.fail();
        deliveryRepository.save(delivery);
    }*/
}
