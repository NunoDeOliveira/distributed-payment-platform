package com.tfg.transferservice.metrics;

import com.tfg.transferservice.model.TransferState;
import com.tfg.transferservice.repository.TransferRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TransferMetrics {

    public TransferMetrics(MeterRegistry meterRegistry,
                             TransferRepository productionRepository) {
        for (TransferState state : TransferState.values()) {
            if (state == TransferState.REJECTED) continue;
            Gauge.builder("production.state.current",
                            productionRepository,
                            repo -> repo.sumAmountByState(state))
                            .tag("state", state.name())
                            .register(meterRegistry);
        }
    }

}
