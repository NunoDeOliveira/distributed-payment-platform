package com.tfg.ledgerservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEvent {
    private String eventType;
    private Long LedgerId;
    private String correlationId;
    private BigDecimal amount;
}
