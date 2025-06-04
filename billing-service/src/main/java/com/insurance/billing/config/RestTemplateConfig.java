package com.insurance.billing.config; // Or your appropriate config package

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // You can customize the RestTemplate here if needed
        // e.g., builder.setConnectTimeout(Duration.ofSeconds(5))
        //          .setReadTimeout(Duration.ofSeconds(5))
        return builder.build();
    }
}