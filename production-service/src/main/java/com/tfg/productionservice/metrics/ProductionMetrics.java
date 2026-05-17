package com.tfg.productionservice.metrics;

import com.tfg.productionservice.model.ProductionState;
import com.tfg.productionservice.repository.ProductionRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ProductionMetrics {

    public ProductionMetrics(MeterRegistry meterRegistry,
                             ProductionRepository productionRepository) {
        for (ProductionState state : ProductionState.values()) {
            Gauge.builder("production.state.current",
                            productionRepository,
                            repo -> repo.sumAmountByState(state))
                            .tag("state", state.name())
                            .register(meterRegistry);
        }
    }

}
