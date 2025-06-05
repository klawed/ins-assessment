package com.insurance.billing.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
class ProfileVerificationTest {

    @Autowired
    private Environment environment;

    @Test
    void shouldUseTestProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        assertThat(activeProfiles).contains("test");

        // Verify H2 database is being used
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        assertThat(datasourceUrl).contains("h2:mem");

        // Verify Flyway is disabled
        Boolean flywayEnabled = environment.getProperty("spring.flyway.enabled", Boolean.class);
        assertThat(flywayEnabled).isFalse();
    }
}
