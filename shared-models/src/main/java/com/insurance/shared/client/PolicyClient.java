package com.insurance.shared.client;

import com.insurance.shared.entity.Policy;
import java.util.Optional;

public interface PolicyClient {
    Optional<Policy> getPolicy(String policyId);
    boolean existsById(String policyId);
}