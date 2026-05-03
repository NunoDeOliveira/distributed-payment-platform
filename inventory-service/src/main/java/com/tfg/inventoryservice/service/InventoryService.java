package com.tfg.inventoryservice.service;

import com.tfg.inventoryservice.message.InventoryPublish;
import com.tfg.inventoryservice.model.StockEntry;
import com.tfg.inventoryservice.repository.InventoryRepository;
import com.tfg.inventoryservice.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import com.tfg.inventoryservice.model.StockReservation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryPublish eventPublish;
    private final ReservationRepository reservationRepository;

    private static final int MAX_STOCK = 30;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryPublish eventPublish,
                            ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublish = eventPublish;
        this.reservationRepository = reservationRepository;
    }

    // Given an ID and a production quantity, validate it.
    public void validateProduction(Long id, int amount){
        // Calculate the stock that can still be added
        int currentStock = getAvailabilityStock();
        int allowedCapacity = MAX_STOCK - currentStock;

        // Evaluate if the amount received is rejected or accepted
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

        inventoryRepository.save(stockEntry);
    }

    //
    @Transactional
    public void confirmDelivery(Long id, int amount){
        StockReservation reservation = reservationRepository.findByReservationId(id);

        if (reservation == null) {
            return;
        }
        //
        StockEntry stockEntry = new StockEntry();
        stockEntry.setProductionId(id);
        stockEntry.setAmount(-reservation.getReservationAmount());

        inventoryRepository.save(stockEntry);
        reservationRepository.delete(reservation);
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
