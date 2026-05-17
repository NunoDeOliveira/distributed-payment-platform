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


@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryPublish eventPublish;
    private final ReservationRepository reservationRepository;
    
    @Value("${inventory.stock.limit:30}")
    private int MAX_STOCK;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryPublish eventPublish,
                            ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublish = eventPublish;
        this.reservationRepository = reservationRepository;
    }
    
    @Transactional
    // Given an ID and a production quantity, validate it.
    public void validateProduction(Long id, int amount){
        // Calculate the stock that can still be added
        int currentStock = getAvailabilityStock();
        int allowedCapacity = MAX_STOCK - currentStock;

        // Evaluate whether the amount received is rejected or accepted
        if (amount <= allowedCapacity){
            eventPublish.publishProductionAccepted(id, amount);
        } else {
            eventPublish.publishProductionRejected(id, allowedCapacity);
        }
    }

    @Transactional
    // Given an ID and a quantity, reserve a stock quantity
    public void validateDelivery(Long id, int amount){
        // Get the stock availability for a production ID
        int availabilityStock = getAvailabilityStock();

        // Check if there are enough stock
        if (amount <= availabilityStock){
            StockReservation reservation = new StockReservation();
            reservation.setReservationId(id);
            reservation.setReservationAmount(amount);

            // Save reservation in DB
            reservationRepository.save(reservation);

            // Publish an event accepting the delivery request
            eventPublish.publishDeliveryAccepted(id, amount);
        } else {
            eventPublish.publishDeliveryRejected(id, availabilityStock);
        }
    }

    // Given an id production method to get the stock availability
    public int getAvailabilityStock(){
        int totalStock = getTotalStock() - getReservedStock();

        return totalStock;
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
    public void increaseStock(Long id, int amount){
        StockEntry stockEntry = new StockEntry();
        stockEntry.setProductionId(id);
        stockEntry.setAmount(amount);

        // Add new production to entry stock
        inventoryRepository.save(stockEntry);
        
        // Notify delivery service that stock is available
        //eventPublish.publishStockAvailable(getAvailabilityStock()); // 16 may from amount to actual
        // Notify production service that capacity is available
        //eventPublish.publishCapacityAvailable(MAX_STOCK - getAvailabilityStock());
    }

    // Given and id of product and amount release a reservation
    @Transactional
    public void confirmDelivery(Long id, int amount){
        // Get reservation from repository
        StockReservation reservation = reservationRepository.findByReservationId(id);
        if (reservation == null) {
            return;
        }
        // Discount delivery from repository
        StockEntry stockEntry = new StockEntry();
        stockEntry.setProductionId(id);
        stockEntry.setAmount(-reservation.getReservationAmount());

        inventoryRepository.save(stockEntry);
        reservationRepository.delete(reservation);
        
        // Notify to production sevice when a delivery is completed
        //eventPublish.publishCapacityAvailable(MAX_STOCK - getAvailabilityStock());
        // Notify delivery service that stock may have changed
        //eventPublish.publishStockAvailable(getAvailabilityStock());
    }

    @Transactional
    public void releaseReservedStock(Long id, int amount){
        // If the order fails, the reservation is rejected
        // so that the stock becomes available again.
        StockReservation reservation = reservationRepository.findByReservationId(id);

        if (reservation != null) {
            reservationRepository.delete(reservation);
        }
    }

}
