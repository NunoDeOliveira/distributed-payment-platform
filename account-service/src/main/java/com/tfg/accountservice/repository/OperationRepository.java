package com.tfg.accountservice.repository;

import com.tfg.accountservice.model.OperationState;
import com.tfg.accountservice.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    //@Query("SELECT SUM(reservation.reservationAmount) FROM BalanceHold reservation")
    //Integer getTotalReservedStock();

    long countByState(OperationState state);

    Optional<Operation> findByCorrelationId(String correlationId);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Operation o WHERE o.state = :state")
    long sumAmountByState(@Param("state") OperationState state);

    //void deleteByReservationId(@Param("state") OperationState state);
}
