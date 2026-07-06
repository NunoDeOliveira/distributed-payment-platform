package com.tfg.accountservice.metrics;

import com.tfg.accountservice.model.BalanceState;
import com.tfg.accountservice.repository.BalanceRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AccountMetrics {

    public AccountMetrics(MeterRegistry meterRegistry, BalanceRepository balanceRepository) {
        for (BalanceState state : BalanceState.values()) {
            if (state == BalanceState.REJECTED) {
                continue;
            }

            Gauge.builder("account.state.current", balanceRepository, repo ->
                                    repo.sumAmountByState(state).doubleValue()).tag("state", state.name())
                                    .register(meterRegistry);
        }
    }

}
