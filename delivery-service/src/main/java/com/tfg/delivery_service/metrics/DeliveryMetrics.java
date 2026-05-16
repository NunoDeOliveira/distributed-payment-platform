package com.tfg.delivery_service.metrics;

import com.tfg.delivery_service.model.DeliveryState;
import com.tfg.delivery_service.repository.DeliveryRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMetrics {

    public DeliveryMetrics(MeterRegistry meterRegistry,
                           DeliveryRepository deliveryRepository) {
        for (DeliveryState state : DeliveryState.values()) {
            Gauge.builder("delivery.state.current",
                            deliveryRepository,
                            repo -> repo.countByState(state))
                            .tag("state", state.name())
                            .register(meterRegistry);
        }
    }

}
