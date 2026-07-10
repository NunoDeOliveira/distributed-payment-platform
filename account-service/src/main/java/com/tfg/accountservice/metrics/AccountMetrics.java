package com.tfg.accountservice.metrics;

import com.tfg.accountservice.model.BalanceState;
import com.tfg.accountservice.repository.BalanceRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AccountMetrics {
    public AccountMetrics(MeterRegistry meterRegistry, BalanceRepository balanceRepository) {
        Gauge.builder("account.balance.current", balanceRepository, repo ->
                        repo.findTopByOrderByIdDesc()
                                .map(b -> b.getBalanceAccount().doubleValue())
                                .orElse(0.0))
                                .description("Current account balance")
                                .register(meterRegistry);
    }
}
