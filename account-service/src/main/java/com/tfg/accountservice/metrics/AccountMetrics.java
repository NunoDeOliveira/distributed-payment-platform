package com.tfg.accountservice.metrics;

import com.tfg.accountservice.model.OperationState;
import com.tfg.accountservice.repository.AccountRepository;
import com.tfg.accountservice.repository.OperationRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AccountMetrics {

    public AccountMetrics(MeterRegistry meterRegistry, OperationRepository operationRepository) {
        for (OperationState state : OperationState.values()) {
            if (state == OperationState.REJECTED) continue;

            Gauge.builder("account.state.current", operationRepository,
                    repo -> repo.sumAmountByState(state)).tag("state", state.name()).register(meterRegistry);
        }
    }

}
