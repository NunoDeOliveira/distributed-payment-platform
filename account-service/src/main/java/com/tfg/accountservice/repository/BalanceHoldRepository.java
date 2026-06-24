package com.tfg.accountservice.repository;

import com.tfg.accountservice.model.BalanceHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BalanceHoldRepository extends JpaRepository<BalanceHold, Long> {
    @Query("SELECT SUM(reservation.reservationAmount) FROM BalanceHold reservation")
    Integer getTotalReservedStock();

    BalanceHold findFirstByReservationId(Long reservationId);

    void deleteByReservationId(Long reservationId);
}
