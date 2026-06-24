package com.tfg.ledgerservice.metrics;

import com.tfg.ledgerservice.model.LedgerMovementState;
import com.tfg.ledgerservice.repository.LedgerMovementRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class LedgerMetrics {

    public LedgerMetrics(MeterRegistry meterRegistry,
                           LedgerMovementRepository deliveryRepository) {
        for (LedgerMovementState state : LedgerMovementState.values()) {
            if (state == LedgerMovementState.REJECTED) continue;
            Gauge.builder("delivery.state.current",
                            deliveryRepository,
                            repo -> repo.sumAmountByState(state))
                            .tag("state", state.name())
                            .register(meterRegistry);
        }
    }

}


