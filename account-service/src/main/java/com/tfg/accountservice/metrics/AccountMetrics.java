package com.tfg.accountservice.metrics;

import com.tfg.paymentservice.model.AccountState;
import com.tfg.paymentservice.repository.AccountRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AccountMetrics {

    public AccountMetrics(MeterRegistry meterRegistry, accountRepository accountRepository) {
        for (AccountState state : AccountState.values()) {
            if (state == AccountState.REJECTED) continue;
            Gauge.builder("account.state.current", accountRepository,
                  repo -> repo.sumAmountByState(state)).tag("state", state.name()).register(meterRegistry);
        }
    }

}
