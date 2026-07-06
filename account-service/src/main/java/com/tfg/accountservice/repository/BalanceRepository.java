package com.tfg.accountservice.repository;

import com.tfg.accountservice.model.Balance;
import com.tfg.accountservice.model.BalanceState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {

    Optional<Balance> findByCorrelationId(String correlationId);

    long countByState(BalanceState state);
}
