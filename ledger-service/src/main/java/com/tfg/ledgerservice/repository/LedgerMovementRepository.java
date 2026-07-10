package com.tfg.ledgerservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.tfg.ledgerservice.model.MovementState;
import com.tfg.ledgerservice.model.Movement;
import java.util.Optional;
import java.util.List;


@Repository
public interface LedgerMovementRepository extends JpaRepository<Movement, Long> {
    Optional<Movement> findFirstByStateOrderByStartTimeAsc(MovementState state);
    // Method of consulting for count state
    long countByState(MovementState state);

    // Added to get all the pendings
    List<Movement> findByStateOrderByStartTimeAsc(MovementState state);

    // Sum amount by state for metrics in units
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM Movement m WHERE m.state = :state")
    long sumAmountByState(@Param("state") MovementState state);

    // Find by Id to cancel a ledger

    Optional<Movement> findByCorrelationId(String correlationId);
    boolean existsByCorrelationId(String correlationId);
}
