package com.insurance.shared.client;

import java.util.Optional;
import com.insurance.shared.dto.PolicyDto;

public interface PolicyClient {
    Optional<PolicyDto> getPolicy(String policyId);
    boolean existsById(String policyId);
}