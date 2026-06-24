package com.tfg.ledgerservice.metrics;

import com.tfg.ledgerservice.model.DeliveryState;
import com.tfg.ledgerservice.repository.DeliveryRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMetrics {

    public DeliveryMetrics(MeterRegistry meterRegistry,
                           DeliveryRepository deliveryRepository) {
        for (DeliveryState state : DeliveryState.values()) {
            if (state == DeliveryState.REJECTED) continue;
            Gauge.builder("delivery.state.current",
                            deliveryRepository,
                            repo -> repo.sumAmountByState(state))
                            .tag("state", state.name())
                            .register(meterRegistry);
        }
    }

}


