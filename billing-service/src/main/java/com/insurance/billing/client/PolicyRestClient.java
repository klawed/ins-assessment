package com.insurance.billing.client;

import com.insurance.shared.client.PolicyClient;
import com.insurance.shared.entity.Policy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PolicyRestClient implements PolicyClient {
    private final RestTemplate restTemplate;
    private final String policyServiceUrl = "${policy.service.url}";
    
    @Override
    public Optional<Policy> getPolicy(String policyId) {
        try {
            Policy policy = restTemplate.getForObject(
                policyServiceUrl + "/api/policies/{id}", 
                Policy.class, 
                policyId
            );
            return Optional.ofNullable(policy);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(String policyId) {
        try {
            return Boolean.TRUE.equals(restTemplate.getForObject(
                policyServiceUrl + "/api/policies/{id}/exists",
                Boolean.class,
                policyId
            ));
        } catch (Exception e) {
            return false;
        }
    }
}