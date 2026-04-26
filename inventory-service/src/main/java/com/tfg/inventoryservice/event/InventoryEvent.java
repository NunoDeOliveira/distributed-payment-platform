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
    private Integer amount;


}
