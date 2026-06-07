package com.minipay.event;

import com.minipay.dto.PaymentEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "minipay.events.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpPaymentEventPublisher implements PaymentEventPublisher {

    @Override
    public void publish(PaymentEvent event) {
    }
}
