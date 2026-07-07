package com.tfg.ledgerservice.service;

import com.tfg.ledgerservice.message.LedgerPublish;
import com.tfg.ledgerservice.model.Movement;
import com.tfg.ledgerservice.model.MovementState;
import com.tfg.ledgerservice.repository.LedgerMovementRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LedgerService {
    // Attributes
    private final LedgerMovementRepository movementRepo;
    private final LedgerPublish ledgerPublish;

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

        // Idempotency
        if (movementRepo.findByCorrelationId(correlationId).isPresent()) {
            return;
        }

        // Create the ledger entry
        Movement movement = new Movement(correlationId, amount, MovementState.RECORDED, LocalDateTime.now());

        try {
            movement.recorded();
            movementRepo.save(movement);
            // Publish  recorded event
            ledgerPublish.publishLedgerMovementRecorded(correlationId, amount);

        } catch (Exception e) {
            movement.failed();
            movementRepo.save(movement);
            // Publish failed event
            ledgerPublish.publishLedgerMovementFailed(correlationId, amount);
        }

    }

    @Transactional
    public void cancelMovement(String correlationId) {
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
        movement.cancelled();
        movementRepo.save(movement);
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
