package com.tfg.accountservice.event;
import com.tfg.accountservice.model.OperationState;
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
    private String correlationId;
    private BigDecimal totalAmount;
    private OperationState state;


}
