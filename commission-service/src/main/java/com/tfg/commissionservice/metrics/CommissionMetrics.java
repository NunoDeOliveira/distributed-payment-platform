package com.tfg.commissionservice.metrics;

import com.tfg.commissionservice.repository.CommissionRepository;
import com.tfg.commissionservice.model.CommissionState;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CommissionMetrics {

    public CommissionMetrics(MeterRegistry meterRegistry, CommissionRepository paymentRepository) {
        for (CommissionState state : CommissionState.values()) {
            if (state == CommissionState.REJECTED) continue;
            Gauge.builder("payment.state.current", paymentRepository, repo
                            -> repo.sumAmountByState(state)).tag("state", state.name()).register(meterRegistry);
        }
    }

}