package com.insurance.payment.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;

@Service
@Slf4j
public class PaymentConsumer implements Consumer<String> {

    @Override
    public void accept(String message) {
        log.info("Received message: {}", message);
        // Add your business logic here
        try {
            // Simulate processing logic
            log.debug("Processing message: {}", message);
            // Add actual processing logic here
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}