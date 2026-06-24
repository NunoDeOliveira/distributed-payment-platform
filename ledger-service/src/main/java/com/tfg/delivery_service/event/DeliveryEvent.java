package com.tfg.delivery_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvent {
    private String eventType;
    private Long deliveryId;
    private Long productionId;
    private Integer amount;
}
