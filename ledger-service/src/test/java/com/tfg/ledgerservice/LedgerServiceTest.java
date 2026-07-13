package com.tfg.ledgerservice;

import com.tfg.ledgerservice.message.LedgerPublish;
import com.tfg.ledgerservice.model.Movement;
import com.tfg.ledgerservice.model.MovementState;
import com.tfg.ledgerservice.repository.LedgerMovementRepository;
import com.tfg.ledgerservice.service.LedgerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerMovementRepository movementRepository;

    @Mock
    private LedgerPublish ledgerPublish;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void shouldRecordMovementAndPublishRecordedEvent() {
        String correlationId = "record-correlation-id";
        BigDecimal amount = new BigDecimal("102.00");

        when(movementRepository.findByCorrelationId(correlationId))
                .thenReturn(Optional.empty());

        when(movementRepository.save(any(Movement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ledgerService.recordMovement(correlationId, amount);

        ArgumentCaptor<Movement> movementCaptor =
                ArgumentCaptor.forClass(Movement.class);

        verify(movementRepository)
                .findByCorrelationId(correlationId);

        verify(movementRepository)
                .save(movementCaptor.capture());

        Movement savedMovement = movementCaptor.getValue();

        assertEquals(correlationId, savedMovement.getCorrelationId());

        assertEquals(
                0,
                savedMovement.getAmount()
                        .compareTo(new BigDecimal("-102.00"))
        );

        assertEquals(MovementState.RECORDED, savedMovement.getState());
        assertNotNull(savedMovement.getStartTime());
        assertTrue(savedMovement.getRegister().contains("RECORDED"));

        verify(ledgerPublish)
                .publishLedgerMovementRecorded(correlationId, amount);
    }

    @Test
    void shouldIgnoreAlreadyRecordedMovement() {
        String correlationId = "duplicate-correlation-id";
        BigDecimal amount = new BigDecimal("102.00");

        Movement existingMovement =
                new Movement(correlationId, amount.negate());

        existingMovement.recorded();

        when(movementRepository.findByCorrelationId(correlationId))
                .thenReturn(Optional.of(existingMovement));

        ledgerService.recordMovement(correlationId, amount);

        verify(movementRepository)
                .findByCorrelationId(correlationId);

        verify(movementRepository, never())
                .save(any(Movement.class));

        verify(ledgerPublish, never())
                .publishLedgerMovementRecorded(any(), any());
    }

    @Test
    void shouldCancelRecordedMovementAndCreateReleasedMovement() {
        String correlationId = "cancel-correlation-id";
        BigDecimal amount = new BigDecimal("102.00");

        Movement recordedMovement =
                new Movement(correlationId, amount.negate());

        recordedMovement.recorded();

        when(movementRepository.findByCorrelationId(correlationId))
                .thenReturn(Optional.of(recordedMovement));

        when(movementRepository.save(any(Movement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ledgerService.cancelMovement(correlationId);

        ArgumentCaptor<Movement> movementCaptor =
                ArgumentCaptor.forClass(Movement.class);

        verify(movementRepository, times(2))
                .save(movementCaptor.capture());

        List<Movement> savedMovements = movementCaptor.getAllValues();

        Movement canceledMovement = savedMovements.get(0);
        Movement releasedMovement = savedMovements.get(1);

        assertEquals(MovementState.CANCELED, canceledMovement.getState());

        assertEquals(
                0,
                canceledMovement.getAmount()
                        .compareTo(new BigDecimal("-102.00"))
        );

        assertTrue(canceledMovement.getRegister().contains("CANCELED"));

        assertEquals(MovementState.RELEASED, releasedMovement.getState());

        assertEquals(
                0,
                releasedMovement.getAmount()
                        .compareTo(new BigDecimal("102.00"))
        );

        assertNotNull(releasedMovement.getEndTime());
        assertTrue(releasedMovement.getRegister().contains("RELEASED"));

        verify(ledgerPublish)
                .publishLedgerMovementCanceled(correlationId, amount);
    }

    @Test
    void shouldCompensateWhenCancellationArrivesBeforeMovement() {
        String correlationId = "out-of-order-correlation-id";
        BigDecimal amount = new BigDecimal("102.00");

        AtomicReference<Movement> storedMovement =
                new AtomicReference<>();

        when(movementRepository.findByCorrelationId(correlationId))
                .thenAnswer(invocation ->
                        Optional.ofNullable(storedMovement.get())
                );

        when(movementRepository.save(any(Movement.class)))
                .thenAnswer(invocation -> {
                    Movement movement = invocation.getArgument(0);

                    if (movement.getState() == MovementState.WAITING) {
                        storedMovement.set(movement);
                    }

                    return movement;
                });

        ledgerService.cancelMovement(correlationId);

        Movement waitingMovement = storedMovement.get();

        assertNotNull(waitingMovement);
        assertEquals(MovementState.WAITING, waitingMovement.getState());
        assertEquals(0, waitingMovement.getAmount().compareTo(BigDecimal.ZERO));
        assertTrue(waitingMovement.getRegister().contains("WAITING"));

        clearInvocations(movementRepository, ledgerPublish);

        ledgerService.recordMovement(correlationId, amount);

        ArgumentCaptor<Movement> movementCaptor =
                ArgumentCaptor.forClass(Movement.class);

        verify(movementRepository)
                .findByCorrelationId(correlationId);

        verify(movementRepository, times(2))
                .save(movementCaptor.capture());

        List<Movement> savedMovements = movementCaptor.getAllValues();

        Movement canceledMovement = savedMovements.get(0);
        Movement releasedMovement = savedMovements.get(1);

        assertEquals(MovementState.CANCELED, canceledMovement.getState());

        assertEquals(
                0,
                canceledMovement.getAmount()
                        .compareTo(new BigDecimal("-102.00"))
        );

        assertEquals(MovementState.RELEASED, releasedMovement.getState());

        assertEquals(
                0,
                releasedMovement.getAmount()
                        .compareTo(new BigDecimal("102.00"))
        );

        verify(ledgerPublish)
                .publishLedgerMovementCanceled(correlationId, amount);

        verify(ledgerPublish, never())
                .publishLedgerMovementRecorded(any(), any());
    }
}
