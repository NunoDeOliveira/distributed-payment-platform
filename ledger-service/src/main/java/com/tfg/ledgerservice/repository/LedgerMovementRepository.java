package com.tfg.ledgerservice.repository;

import com.tfg.ledgerservice.model.LedgerMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tfg.ledgerservice.model.LedgerMovementState;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LedgerMovementRepository extends JpaRepository<LedgerMovement, Long> {
    Optional<LedgerMovement> findFirstByStateOrderByStartTimeAsc(LedgerMovementState state);
    // Method of consulting for count state
    long countByState(LedgerMovementState state);
    // Added to get all the pendings
    List<LedgerMovement> findByStateOrderByStartTimeAsc(LedgerMovementState state);
    // Sum amount by state for metrics in units
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM LedgerMovement d WHERE d.state = :state")
    long sumAmountByState(@Param("state") LedgerMovementState state);
    // Find by Id to cancell a delivery
    Optional<LedgerMovement> findByProductionId(Long productionId);
}
