package com.minipay.event;

import com.minipay.dto.PaymentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "minipay.events.kafka.enabled", havingValue = "true")
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final String topic;

    public KafkaPaymentEventPublisher(KafkaTemplate<String, PaymentEvent> kafkaTemplate,
                                      @Value("${minipay.events.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(PaymentEvent event) {
        kafkaTemplate.send(topic, event.transactionId().toString(), event);
    }
}
