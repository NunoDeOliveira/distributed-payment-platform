package com.tfg.ledgerservice.service;

import com.tfg.ledgerservice.message.LedgerPublish;
import com.tfg.ledgerservice.model.Movement;
import com.tfg.ledgerservice.model.MovementState;
import com.tfg.ledgerservice.repository.LedgerMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class LedgerService {
    // Attributes
    private final LedgerMovementRepository movementRepo;
    private final LedgerPublish ledgerPublish;
    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);
    // Constructor
    public LedgerService(LedgerMovementRepository movementRepo, LedgerPublish ledgerPublish) {
        this.movementRepo = movementRepo;
        this.ledgerPublish = ledgerPublish;
    }


    // Given an ID and amount of a LedgerMovement recorded a movement
    @Transactional
    public void recordMovement(String correlationId, BigDecimal amount) {
        if (correlationId == null || amount == null) {
            return;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        Movement movement = movementRepo.findByCorrelationId(correlationId).orElse(null);

        // Check if there are movements in pending state before recorded
        if (movement != null && movement.getState() == MovementState.WAITING) {
            movement.setAmount(amount.negate()); // negative amount
            movement.cancelled();
            movementRepo.save(movement);
            // create a new row to RELEASED operation
            releaseMovement(correlationId, amount);
            return;
        }

        if (movement != null) {
            log.info("recordMovement | already exists | correlationId={}", correlationId);
            return;
        }

        // If it does not exist this movement update to recorded state
        Movement recorded = new Movement(correlationId, amount.negate());
        recorded.recorded();
        movementRepo.save(recorded);
        ledgerPublish.publishLedgerMovementRecorded(correlationId, amount);
        log.info("recordMovement | recorded | correlationId={} | amount={}",
                                            correlationId, amount.negate());
    }

    // Given an correlationID of an operation and an amount of that operation, cancel movement
    @Transactional
    public void cancelMovement(String correlationId) {
        if (correlationId == null) return;

        Movement movement = movementRepo.findByCorrelationId(correlationId).orElse(null);

        // Case 1: If this operation does not exist or has not yet arrived, update as WAITING
        if (movement == null) {
            Movement waiting = new Movement(correlationId, BigDecimal.ZERO);
            waiting.waiting();
            movementRepo.save(waiting);
            log.info("cancelMovement | saved as WAITING | correlationId={}", correlationId);
            return;
        }

        // Case 2: Already exists
        if (movement.getState() == MovementState.CANCELED) {
            log.info("cancelMovement | already CANCELED | correlationId={}", correlationId);
            return;
        }

        // Case 3: Exists and will be canceled
        if (movement.getState() == MovementState.RECORDED) {
            movement.cancelled();
            movementRepo.save(movement);
            releaseMovement(correlationId, movement.getAmount().abs());
            log.info("cancelMovement | canceled and released | correlationId={}", correlationId);
        }
    }

    // Given a correlation of operation ID and an amount of the same operation, release amount
    @Transactional
    public void releaseMovement(String correlationId, BigDecimal amount) {
        if (correlationId == null || amount == null) {
            return;
        }

        Movement returned = new Movement(correlationId, amount);
        returned.released();
        movementRepo.save(returned);
        ledgerPublish.publishLedgerMovementCanceled(correlationId, amount);
        log.info("releaseMovement | released | correlationId={} | amount={}", correlationId, amount);
    }

    // Given an
    @Transactional
    public void rejectMovement(Long ledgerId, String correlationId) {
        // Check input data
        if (correlationId == null) {
            return;
        }

        // Get existing movement
        Movement movement = (Movement) movementRepo.findByCorrelationId(correlationId).orElse(null);
        if (movement == null) {
            return;
        }

        // Check the state before cancel
        MovementState movementToCancel = movement.getState();
        if (movementToCancel != MovementState.RECORDED) {
            return;
        }

        // If there are no errors, update state as canceled, save the state and publish the event
        movement.reject();
        movementRepo.save(movement);
        ledgerPublish.publishLedgerMovementRejected(ledgerId, correlationId, movement.getAmount());
    }

    // Get delivery by ID
    public Movement getLedgerMovement(Long id) {
        return movementRepo.findById(id).orElseThrow(() -> new RuntimeException("Movement " + id + " not found"));
    }
            
    // Get all the movements from the repository.
    public List<Movement> getAllMovements() {
        return movementRepo.findAll();
    }


}
