package com.minipay.event;

import com.minipay.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "minipay.events.kafka.consumer.enabled", havingValue = "true")
public class LoggingPaymentEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPaymentEventConsumer.class);

    @KafkaListener(topics = "${minipay.events.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PaymentEvent event) {
        LOGGER.info("Payment event received: eventType={}, transactionId={}, status={}",
                event.eventType(), event.transactionId(), event.status());
    }
}
