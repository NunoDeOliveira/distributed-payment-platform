package com.tfg.inventoryservice.event;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {
    private String eventType;
    private Long productionId;
    private Long deliveryId; // added 17 may
    private Integer amount;


}
