package com.tfg.delivery_service.service;

import com.tfg.delivery_service.message.DeliveryPublish;
import com.tfg.delivery_service.model.Delivery;
import com.tfg.delivery_service.model.DeliveryState;
import com.tfg.delivery_service.repository.DeliveryRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
    public Delivery createDelivery(int amount) {
        Delivery newDelivery = new Delivery(amount,
                          DeliveryState.PENDING, LocalDateTime.now());
        Delivery storedDelivery = deliveryRepository.save(newDelivery);

        deliveryPublish.publishDeliveryCreated(
                      storedDelivery.getId(), storedDelivery.getAmount());

        return storedDelivery;
    }

    // Given an ID of delivery from the RabbitMQ, start a new delivery
    @Async
    public void startDelivery(Long id) {
        Delivery delivery = getDelivery(id);
        delivery.start();
        // Update state in database
        deliveryRepository.save(delivery);
        
        try {
            // Wait 60 seconds to send delivery completed
            Thread.sleep(60000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Apply the logic to finish delivery
        completeDelivery(id);
    }

    // Method for saving a rejected delivery in the DB
    public void rejectDelivery(Long id) {
        Delivery delivery = getDelivery(id);
        delivery.reject();
        deliveryRepository.save(delivery);
    }

    // Saga compensating transaction method.
    // Given a rejected delivery and the maximum amount allowed for that delivery
    public void compensateRejectedDelivery(Long rejectedDeliveryId, int maxAllowedAmount) {
        // Update the delivery state to reject
        Delivery delivery = getDelivery(rejectedDeliveryId);
        delivery.reject();
        deliveryRepository.save(delivery);

        // Create a new delivery limited by the given amount
        if (maxAllowedAmount > 0) {
            createDelivery(maxAllowedAmount);
        }
    }

    // When the delivery is completed save delivery in the repository
    // and publish an event on RabbitMQ
    public void completeDelivery(Long id) {
        Delivery deliveryCompleted = getDelivery(id);
        deliveryCompleted.complete();
        Delivery storedDelivery = deliveryRepository.save(deliveryCompleted);

        // Method to send event to RabbitMQ
        deliveryPublish.publishDeliveryCompleted(
                        storedDelivery.getId(), storedDelivery.getAmount());

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


}
