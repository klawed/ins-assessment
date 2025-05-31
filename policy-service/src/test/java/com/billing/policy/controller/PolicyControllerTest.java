package com.billing.policy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnHelloMessage() throws Exception {
        mockMvc.perform(get("/api/policies/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.service", is("policy-service")))
                .andExpect(jsonPath("$.message", is("Hello from Policy Service!")))
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    void shouldReturnPolicyById() throws Exception {
        String policyId = "POLICY-123";

        mockMvc.perform(get("/api/policies/{policyId}", policyId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.policyId", is(policyId)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void shouldReturnPolicySchedule() throws Exception {
        String policyId = "POLICY-123";

        mockMvc.perform(get("/api/policies/{policyId}/schedule", policyId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.policyId", is(policyId)))
                .andExpect(jsonPath("$.premiumSchedule", is("Monthly - $100")));
    }
}