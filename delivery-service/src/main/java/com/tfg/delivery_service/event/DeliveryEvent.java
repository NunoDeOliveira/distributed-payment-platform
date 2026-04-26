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
    private String event;
    private Long deliveryId;
    private Integer amount;
}
