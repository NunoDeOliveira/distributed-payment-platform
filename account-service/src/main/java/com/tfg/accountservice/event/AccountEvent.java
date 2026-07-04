package com.tfg.accountservice.event;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.math.BigDecimal;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountEvent {
    private String eventType;
    private Long paymentId;
    private String correlationId;
    private BigDecimal amount;
    private Long accountId;


}
