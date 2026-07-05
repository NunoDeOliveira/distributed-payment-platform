package com.tfg.ledgerservice.metrics;

import com.tfg.ledgerservice.model.MovementState;
import com.tfg.ledgerservice.repository.LedgerMovementRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;


@Component
public class LedgerMetrics {

    public LedgerMetrics(MeterRegistry meterRegistry, LedgerMovementRepository movementRepository) {
        for (MovementState state : MovementState.values()) {
            if (state == MovementState.REJECTED) continue;
            Gauge.builder("ledger.state.current", movementRepository, repo ->
                            repo.sumAmountByState(state)).tag("state", state.name()).register(meterRegistry);
        }
    }

}


