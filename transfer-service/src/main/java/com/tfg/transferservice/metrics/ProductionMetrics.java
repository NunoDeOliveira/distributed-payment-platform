package com.tfg.transferservice.metrics;

import com.tfg.transferservice.model.ProductionState;
import com.tfg.transferservice.repository.ProductionRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ProductionMetrics {

    public ProductionMetrics(MeterRegistry meterRegistry,
                             ProductionRepository productionRepository) {
        for (ProductionState state : ProductionState.values()) {
            if (state == ProductionState.REJECTED) continue;
            Gauge.builder("production.state.current",
                            productionRepository,
                            repo -> repo.sumAmountByState(state))
                            .tag("state", state.name())
                            .register(meterRegistry);
        }
    }

}
