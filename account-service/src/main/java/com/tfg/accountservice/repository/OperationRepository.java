package com.tfg.accountservice.repository;

import com.tfg.accountservice.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    @Query("SELECT SUM(reservation.reservationAmount) FROM BalanceHold reservation")
    Integer getTotalReservedStock();

    Optional<Operation> findByCorrelationId(String correlationId);

    void deleteByReservationId(Long reservationId);
}
