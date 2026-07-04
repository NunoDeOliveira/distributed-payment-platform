package com.tfg.ledgerservice.repository;

import com.tfg.ledgerservice.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tfg.ledgerservice.model.MovementState;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LedgerMovementRepository extends JpaRepository<Movement, Long> {
    Optional<Movement> findFirstByStateOrderByStartTimeAsc(MovementState state);
    // Method of consulting for count state
    long countByState(MovementState state);
    // Added to get all the pendings
    List<Movement> findByStateOrderByStartTimeAsc(MovementState state);
    // Sum amount by state for metrics in units
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM LedgerMovement d WHERE d.state = :state")
    long sumAmountByState(@Param("state") MovementState state);
    // Find by Id to cancell a delivery
    Optional<Movement> findByProductionId(Long productionId);



    Optional<Object> findByCorrelationId(String correlationId);
}
