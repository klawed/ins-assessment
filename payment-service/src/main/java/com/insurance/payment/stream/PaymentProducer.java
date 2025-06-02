package com.insurance.payment.stream;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private final StreamBridge streamBridge;

    public PaymentProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendPaymentEvent(Object paymentEvent) {
        streamBridge.send("payment-out", paymentEvent);
    }
}