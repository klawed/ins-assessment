package com.billing.policy.service;

import com.billing.shared.dto.PolicyDto;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface PolicyService {
    Optional<PolicyDto> getPolicyById(String policyId);
    List<PolicyDto> getPoliciesByCustomerId(String customerId);
    Map<String, Object> getPremiumSchedule(String policyId);
}