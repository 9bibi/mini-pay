package com.minipay.event;

import com.minipay.dto.PaymentEvent;

public interface PaymentEventPublisher {

    void publish(PaymentEvent event);
}
