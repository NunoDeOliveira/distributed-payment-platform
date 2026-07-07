package com.tfg.ledgerservice.event;

import com.tfg.ledgerservice.model.MovementState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LedgerEvent {
    private String eventType;
    private String correlationId;
    private BigDecimal amount;
    private Long ledgerId;
}
