package com.tfg.inventoryservice.service;

import com.tfg.inventoryservice.message.InventoryPublish;
import com.tfg.inventoryservice.model.StockEntry;
import com.tfg.inventoryservice.repository.InventoryRepository;
import com.tfg.inventoryservice.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import com.tfg.inventoryservice.model.StockReservation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.util.Optional;
import org.springframework.transaction.annotation.Isolation;


@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryPublish eventPublish;
    private final ReservationRepository reservationRepository;
    
    @Value("${inventory.stock.limit:100}")
    private int MAX_STOCK;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryPublish eventPublish,
                            ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublish = eventPublish;
        this.reservationRepository = reservationRepository;
    }
    
    // Given an ID and a production quantity, validate it.
    //@Transactional
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized void validateProduction(Long id, int amount) {  
        if (amount <= 0) {
            return;
        }

        // Calculate the stock that can still be added
        int currentStock = getTotalStock();
        int allowedCapacity = MAX_STOCK - currentStock;

        // Evaluate whether the amount received is rejected or accepted
        if (amount <= allowedCapacity) {
            eventPublish.publishProductionAccepted(id, amount);
            eventPublish.publishStockAvailable(id, amount);
        } else {            
            int allowedAmount = allowedCapacity;
            if (allowedAmount < 0) {
                allowedAmount = 0;
            }
            // publish allowed amount if the amount is => than allowed Capacity
            eventPublish.publishProductionRejected(id, allowedAmount);         
            // Check if there is enough stock already stored
            int availableStock = getAvailabilityStock();
            // If there is enough stock, create a new delivery 
            if (availableStock >= amount) {
                eventPublish.publishForCreateDelivery(amount);
            }    
        }
    }
       
    // Given an ID and a quantity, reserve a stock quantity
    //@Transactional
    //public void validateDelivery(Long id, int amount){
    @Transactional(isolation = Isolation.SERIALIZABLE) 
    public void reserveDeliveryStock(Long id, int amount) {
        if (amount <= 0) {
            return;
        }
        
        // Check if the reserved already exists
        StockReservation checkReservation =
                          reservationRepository.findFirstByReservationId(id);
        if (checkReservation != null) {
            return;
        }
        
        // Create a new reservation for an delivery given
        StockReservation reservation = new StockReservation(); 
        reservation.setReservationId(id);
        reservation.setReservationAmount(amount);
        // Save reservation in DB
        reservationRepository.save(reservation);
        // Publish an event accepting the delivery request
        eventPublish.publishDeliveryAccepted(id, amount);
    }

    // Given an id production method to get the stock availability
    public int getAvailabilityStock() {
        int availableStock = getTotalStock() - getReservedStock();
        return availableStock;
    }

    public int getTotalStock() {
        return Optional.ofNullable(inventoryRepository.
                        getTotalStock()).orElse(0);
    }

    public int getReservedStock() {
        return Optional.ofNullable(reservationRepository.
                getTotalReservedStock()).orElse(0);
    }

    @Transactional
    // Given an id and amount of a Production increase the stock of DB
    public void increaseStock(Long id, int amount) {
        if (amount <= 0) {
            return;
        }

        StockEntry stockEntry = new StockEntry();
        stockEntry.setProductionId(id);
        stockEntry.setAmount(amount);
        // Add new production to entry stock
        inventoryRepository.save(stockEntry);
        // Publish in queue that there is available stock
        //eventPublish.publishStockAvailable(id, amount);
    }
    

    // Given and id of product and amount release a reservation
    @Transactional
    public void confirmDelivery(Long id, int amount) {
        // Get reservation from repository
        StockReservation reservation = reservationRepository
                                        .findFirstByReservationId(id);
        if (reservation == null) {
            return;
        }
        
        int reservedAmount = reservation.getReservationAmount();
        
        // Discount delivery from repository
        StockEntry stockEntry = new StockEntry();
        stockEntry.setProductionId(id);
        stockEntry.setAmount(-reservedAmount);

        inventoryRepository.save(stockEntry);
        reservationRepository.delete(reservation);
        
        // Notify to production sevice when a delivery is completed
        eventPublish.publishCapacityAvailable(MAX_STOCK - getAvailabilityStock());
    }

    @Transactional
    public void releaseReservedStock(Long id, int amount) {
        // If the order fails, the reservation is rejected
        // so that the stock becomes available again.
        StockReservation reservation = reservationRepository
                                        .findFirstByReservationId(id);
        if (reservation != null) {
            reservationRepository.delete(reservation);
        }
    }
    
    //Given an id of delivery and amount, cancell delivery
    public void cancelDelivery(Long deliveryId, Long productionId, int amount) {
        // Release stock reserved
        releaseReservedStock(deliveryId, amount);
        eventPublish.publishProductionCancelled(productionId, amount);
    }
    
    public void cancelProduction(Long productionId, int amount) {
        releaseReservedStock(productionId, amount);
        eventPublish.publishDeliveryCancelledByProduction(productionId, amount);
    }
    
    
}
