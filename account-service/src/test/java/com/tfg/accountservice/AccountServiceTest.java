package com.tfg.accountservice;

import com.tfg.accountservice.message.AccountPublish;
import com.tfg.accountservice.model.Balance;
import com.tfg.accountservice.model.BalanceState;
import com.tfg.accountservice.repository.BalanceRepository;
import com.tfg.accountservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountPublish accountPublish;

    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldReserveAmountAndPublishReservedEvent() {
        String correlationId = "reserve-correlation-id";
        BigDecimal currentAmount = new BigDecimal("1000.00");
        BigDecimal amountToReserve = new BigDecimal("102.00");

        Balance currentBalance = new Balance();
        currentBalance.setId(1L);
        currentBalance.setBalanceAccount(currentAmount);

        when(balanceRepository.findTopByOrderByIdDesc())
                .thenReturn(Optional.of(currentBalance));

        when(balanceRepository.save(any(Balance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        accountService.reserveAmount(correlationId, amountToReserve);

        ArgumentCaptor<Balance> balanceCaptor =
                ArgumentCaptor.forClass(Balance.class);

        verify(balanceRepository).findTopByOrderByIdDesc();
        verify(balanceRepository).save(balanceCaptor.capture());

        Balance savedBalance = balanceCaptor.getValue();

        assertEquals(correlationId, savedBalance.getCorrelationId());
        assertEquals(
                0,
                savedBalance.getBalanceAccount()
                        .compareTo(new BigDecimal("898.00"))
        );
        assertEquals(BalanceState.RESERVED, savedBalance.getState());
        assertNotNull(savedBalance.getTime());
        assertTrue(savedBalance.getRegister().contains("RESERVED"));

        verify(accountPublish)
                .publishAmountReserved(correlationId, amountToReserve);

        verify(accountPublish, never())
                .publishAmountRejected(any());
    }

    @Test
    void shouldRejectReservationWhenBalanceIsInsufficient() {
        String correlationId = "rejected-correlation-id";
        BigDecimal currentAmount = new BigDecimal("50.00");
        BigDecimal amountToReserve = new BigDecimal("102.00");

        Balance currentBalance = new Balance();
        currentBalance.setId(1L);
        currentBalance.setBalanceAccount(currentAmount);

        when(balanceRepository.findTopByOrderByIdDesc())
                .thenReturn(Optional.of(currentBalance));

        accountService.reserveAmount(correlationId, amountToReserve);

        verify(balanceRepository).findTopByOrderByIdDesc();

        verify(balanceRepository, never())
                .save(any(Balance.class));

        verify(accountPublish)
                .publishAmountRejected(correlationId);

        verify(accountPublish, never())
                .publishAmountReserved(any(), any());
    }

    @Test
    void shouldCancelReservationAndRestoreBalance() {
        String correlationId = "cancel-correlation-id";
        BigDecimal amountToRestore = new BigDecimal("102.00");

        Balance reservedBalance = new Balance(
                correlationId,
                new BigDecimal("898.00"),
                BalanceState.RESERVED
        );

        when(balanceRepository.findByCorrelationId(correlationId))
                .thenReturn(Optional.of(reservedBalance));

        when(balanceRepository.save(any(Balance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        accountService.cancelReserveAmount(
                correlationId,
                amountToRestore
        );

        ArgumentCaptor<Balance> balanceCaptor =
                ArgumentCaptor.forClass(Balance.class);

        verify(balanceRepository)
                .findByCorrelationId(correlationId);

        verify(balanceRepository)
                .save(balanceCaptor.capture());

        Balance savedBalance = balanceCaptor.getValue();

        assertEquals(
                0,
                savedBalance.getBalanceAccount()
                        .compareTo(new BigDecimal("1000.00"))
        );
        assertEquals(BalanceState.CANCELED, savedBalance.getState());
        assertNotNull(savedBalance.getTime());
        assertTrue(savedBalance.getRegister().contains("CANCELED"));

        verify(accountPublish)
                .publishAmountCanceled(
                        correlationId,
                        amountToRestore
                );
    }
}
