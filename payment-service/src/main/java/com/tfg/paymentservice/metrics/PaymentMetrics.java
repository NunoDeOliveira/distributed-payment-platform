package com.tfg.paymentservice.metrics;

import com.tfg.paymentservice.model.PaymentState;
import com.tfg.paymentservice.repository.PaymentRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {

    public PaymentMetrics(MeterRegistry meterRegistry,
                             PaymentRepository paymentRepository) {
        for (PaymentState state : PaymentState.values()) {
            if (state == PaymentState.REJECTED) continue;
            Gauge.builder("payment.state.current",
                            paymentRepository,
                            repo -> repo.sumAmountByState(state))
                            .tag("state", state.name())
                            .register(meterRegistry);
        }
    }

}
