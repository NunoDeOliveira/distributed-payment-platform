package com.tfg.commissionservice;

import com.tfg.commissionservice.message.CommissionPublish;
import com.tfg.commissionservice.model.Commission;
import com.tfg.commissionservice.model.CommissionMethod;
import com.tfg.commissionservice.model.CommissionState;
import com.tfg.commissionservice.repository.CommissionRepository;
import com.tfg.commissionservice.service.CommissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionServiceTest {

    @Mock
    private CommissionRepository commissionRepository;

    @Mock
    private CommissionPublish commissionPublish;

    @InjectMocks
    private CommissionService commissionService;

    @Test
    void shouldCalculateCommissionAndPublishCalculatedEvent() {
        String correlationId = "test-correlation-id";
        BigDecimal amount = new BigDecimal("100.00");
        String paymentMethod = "INTERNATIONAL_TRANSFER";

        when(commissionRepository.existsByCorrelationId(correlationId)).thenReturn(false);

        when(commissionRepository.save(any(Commission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        commissionService.calculateCommission(correlationId,amount,paymentMethod);

        ArgumentCaptor<Commission> commissionCaptor = ArgumentCaptor.forClass(Commission.class);

        verify(commissionRepository)
                .existsByCorrelationId(correlationId);

        verify(commissionRepository)
                .save(commissionCaptor.capture());

        Commission savedCommission = commissionCaptor.getValue();

        assertEquals(correlationId, savedCommission.getCorrelationId());
        assertEquals(amount, savedCommission.getAmount());
        assertEquals(
                0,
                savedCommission.getCommissionAmount()
                        .compareTo(new BigDecimal("2.00"))
        );
        assertEquals(
                0,
                savedCommission.getTotalAmount()
                        .compareTo(new BigDecimal("102.00"))
        );
        assertEquals(
                CommissionMethod.INTERNATIONAL_TRANSFER,
                savedCommission.getMethod()
        );
        assertEquals(
                CommissionState.CALCULATED,
                savedCommission.getState()
        );
        assertNotNull(savedCommission.getTime());
        assertTrue(savedCommission.getRegister().contains("CALCULATED"));

        /*
         * The current event contract sends totalAmount in both numeric
         * parameters. This behavior is intentionally preserved.
         */
        verify(commissionPublish).publishCommissionCalculated(
                eq(correlationId),
                argThat(value ->
                        value.compareTo(new BigDecimal("102.00")) == 0),
                argThat(value ->
                        value.compareTo(new BigDecimal("102.00")) == 0),
                eq(paymentMethod)
        );

        verifyNoMoreInteractions(
                commissionRepository,
                commissionPublish
        );
    }

    @Test
    void shouldIgnoreAlreadyProcessedCorrelationId() {
        String correlationId = "existing-correlation-id";

        when(commissionRepository.existsByCorrelationId(correlationId))
                .thenReturn(true);

        commissionService.calculateCommission(
                correlationId,
                new BigDecimal("100.00"),
                "INTERNATIONAL_TRANSFER"
        );

        verify(commissionRepository)
                .existsByCorrelationId(correlationId);

        verify(commissionRepository, never())
                .save(any(Commission.class));

        verifyNoInteractions(commissionPublish);
    }
}
