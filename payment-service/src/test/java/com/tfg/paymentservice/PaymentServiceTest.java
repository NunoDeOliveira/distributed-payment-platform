package com.tfg.paymentservice;

import com.tfg.paymentservice.message.PaymentPublish;
import com.tfg.paymentservice.model.Payment;
import com.tfg.paymentservice.model.PaymentMethod;
import com.tfg.paymentservice.model.PaymentState;
import com.tfg.paymentservice.repository.PaymentRepository;
import com.tfg.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentPublish paymentPublish;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldCreatePaymentAndPublishCreatedEvent() {
        BigDecimal amount = new BigDecimal("100.00");
        PaymentMethod method = PaymentMethod.INTERNATIONAL_TRANSFER;

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.setId(1L);
                    return payment;
        });

        Payment result = paymentService.createPayment(amount, method);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(amount, result.getAmount());
        assertEquals(method, result.getMethod());
        assertEquals(PaymentState.CREATED, result.getState());
        assertNotNull(result.getCorrelationId());
        assertFalse(result.getCorrelationId().isBlank());
        assertNotNull(result.getStartAt());
        assertNull(result.getEndAt());

        ArgumentCaptor<Payment> paymentCaptor =
                ArgumentCaptor.forClass(Payment.class);

        verify(paymentRepository, times(1))
                .save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();

        assertEquals(PaymentState.CREATED, savedPayment.getState());
        assertEquals(amount, savedPayment.getAmount());

        verify(paymentPublish, times(1)).publishPaymentCreated(result.getId(), result.getCorrelationId(), result.getAmount(), result.getMethod());

        verifyNoMoreInteractions(paymentRepository, paymentPublish);
    }
}
