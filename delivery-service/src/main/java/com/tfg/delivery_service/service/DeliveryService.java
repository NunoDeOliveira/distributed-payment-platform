package com.tfg.delivery_service.service;

import com.tfg.delivery_service.message.DeliveryPublish;
import com.tfg.delivery_service.model.Delivery;
import com.tfg.delivery_service.model.DeliveryState;
import com.tfg.delivery_service.repository.DeliveryRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPublish deliveryPublish;

    public DeliveryService(DeliveryRepository deliveryRepository, 
                                              DeliveryPublish deliveryPublish) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryPublish = deliveryPublish;
    }
    
    // Given an ID and amount of a Delivery create a delivery
    public Delivery reserveDelivery(Long productionId, int amount) {
        Delivery newDelivery = new Delivery(amount, DeliveryState.RESERVED,
                                                    LocalDateTime.now());

        newDelivery.setProductionId(productionId);
        Delivery storedDelivery = deliveryRepository.save(newDelivery);
        deliveryPublish.publishDeliveryCreated(storedDelivery.getId(), 
                                                storedDelivery.getAmount());

        return storedDelivery;
    }
    
    @Async
    // Given an ID of delivery from the RabbitMQ, start a new delivery
    public void startDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null || delivery.getState() != DeliveryState.RESERVED) {
            return;
        }
        
        delivery.start();
        // Update state in database
        deliveryRepository.save(delivery);
        
        try {
            // Wait 0,001 seconds to send delivery completed
            Thread.sleep(100); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Apply the logic to finish delivery
        completeDelivery(deliveryId);
    }
    
    @Transactional
    // When the delivery is completed save delivery in the repository
    // and publish an event on RabbitMQ
    public void completeDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        // Chech if the delivery received is cancelled
        // the delivery cancelled cannot save as completed delivery 
        if (delivery == null || delivery.getState() != DeliveryState.ON_DELIVERY) {
            return;
        }
    
        delivery.complete();
        deliveryRepository.save(delivery);
        // Method to send event to RabbitMQ
        deliveryPublish.publishDeliveryCompleted(delivery.getId(), delivery.getAmount());
    }
    
    // Given an amount create a delivery from stock already available in Inventory
    public Delivery createDeliveryFromStock(int amount) {
        if (amount <= 0) {
            return null;
        }
    
        Delivery newDelivery = new Delivery(amount, DeliveryState.CREATED,LocalDateTime.now());
        Delivery storedDelivery = deliveryRepository.save(newDelivery);
        if (storedDelivery == null) {
            return null;
        }
        
        // Check if the delivery is saved corretly befor to switch to reserved
        // The create state is only for register
        if (storedDelivery.getState() == DeliveryState.CREATED) {
            storedDelivery.setState(DeliveryState.RESERVED);
            deliveryRepository.save(storedDelivery);
        }
        
        // if the delivery is cancelled publish
        deliveryPublish.publishDeliveryCreated(
                        storedDelivery.getId(), storedDelivery.getAmount());

        return storedDelivery;
    }
    

    // Get delivery by ID
    public Delivery getDelivery(Long id) {
        Optional<Delivery> deliver = deliveryRepository.findById(id);
        Delivery deliveryToReturn = deliver.orElseThrow(()
                -> new RuntimeException("Delivery " + id + "not found"));

        return deliveryToReturn;
    }
            
    // Get all the deliveries from the repository.
    // This query is to return to the user.
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }
    
    // 
    
    /*
    // This method publish start a delivivery
    public void processPendingDeliveries() {
        Optional<Delivery> pending = deliveryRepository
                .findFirstByStateOrderByStartTimeAsc(DeliveryState.PENDING);
                
        if (pending.isPresent()) {
            deliveryPublish.publishDeliveryPending(
                            pending.get().getId(), pending.get().getAmount());
        }
    }*/
    
    @Transactional
    // Method for saving a cancelled delivery in the DB
    public void cancelDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null || delivery.getState() == DeliveryState.COMPLETED ||
            delivery.getState() == DeliveryState.CANCELLED) {
            return;
        }
        // Save delivery state
        delivery.cancelled();
        deliveryRepository.save(delivery);
        // apply compensate transaction
        compensateCancelledDelivery(deliveryId);
    }

    // Saga compensating transaction method.
    // Given a cancelled delivery release delivery reserved
    public void compensateCancelledDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            return;
        }
        
        deliveryPublish.publishReservationRelease(delivery.getId(), delivery.getAmount());
        deliveryPublish.publishDeliveryCancelled(
                        delivery.getId(), delivery.getProductionId(), delivery.getAmount());
    }
    
    
    public void cancelDeliveryByProductionId(Long productionId) {
        Delivery delivery = deliveryRepository.findByProductionId(productionId).orElse(null);
        if (delivery == null) {
            return;
        }
        cancelDelivery(delivery.getId());
    }
    
    // If inventory connection fail get timeout state
    public void getTimeoutState(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            return;
        }
        delivery.timeout();
        deliveryRepository.save(delivery);
    }
    

    /*
    // When the third retry fails, the state is failed
    public void getFailedSate(Delivery delivery) {
        delivery.fail();
        deliveryRepository.save(delivery);
    }*/
}
