package com.tfg.inventoryservice.repository;

import com.tfg.inventoryservice.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<StockReservation, Long> {
    @Query("SELECT SUM(reservation.reservationAmount) FROM StockReservation reservation")
    Integer getTotalReservedStock();

    StockReservation findByReservationId(Long reservationId);

    void deleteByReservationId(Long reservationId);
}
