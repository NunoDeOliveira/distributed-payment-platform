package com.tfg.accountservice.repository;

import com.tfg.accountservice.model.Balance;
import com.tfg.accountservice.model.BalanceState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {

    Optional<Balance> findByCorrelationId(String correlationId);
    long countByState(BalanceState state);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Balance b WHERE b.state IN :states")
    BigDecimal sumAmountByStateIn(@Param("states") Collection<BalanceState> states);

    @Query("SELECT COALESCE(SUM(b.amount), 0) " +  "FROM Balance b " +
            "WHERE b.state IS NULL OR b.state IN :states")
    BigDecimal calculateAvailableBalance(@Param("states") Collection<BalanceState> states);


    double sumAmountByState(BalanceState state);
}
