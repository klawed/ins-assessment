package com.insurance.billing.client;

import com.insurance.shared.client.PolicyClient;
import com.insurance.shared.entity.Policy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // For logging
import org.springframework.beans.factory.annotation.Value; // For injecting properties
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException; // More general exception
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j // Added for logging
@Component
@RequiredArgsConstructor // This handles constructor injection for final fields
public class PolicyRestClient implements PolicyClient {

    private final RestTemplate restTemplate; // Final, will be injected by constructor due to @RequiredArgsConstructor

    @Value("${policy.service.url}") // Inject the property value
    private String policyServiceUrl;

    @Override
    public Optional<Policy> getPolicy(String policyId) {
        String url = policyServiceUrl + "/api/policies/" + policyId; // Use a local variable for clarity
        try {
            log.debug("Fetching policy with ID: {} from URL: {}", policyId, url);
            Policy policy = restTemplate.getForObject(url, Policy.class);
            return Optional.ofNullable(policy);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Policy not found for ID: {} at URL: {}. Status: {}", policyId, url, e.getStatusCode());
            return Optional.empty(); // Specifically handle 404
        } catch (RestClientException e) { // Catch more specific Spring exceptions
            log.error("Error fetching policy with ID: {} from URL: {}. Message: {}", policyId, url, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(String policyId) {
        String url = policyServiceUrl + "/api/policies/" + policyId + "/exists";
        try {
            log.debug("Checking existence for policy ID: {} at URL: {}", policyId, url);
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (RestClientException e) {
            log.error("Error checking existence for policy ID: {} at URL: {}. Message: {}", policyId, url, e.getMessage());
            return false;
        }
    }
}