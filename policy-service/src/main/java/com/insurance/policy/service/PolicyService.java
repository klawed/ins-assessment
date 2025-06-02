package com.insurance.policy.service;

import com.insurance.shared.dto.PolicyDto;
import com.insurance.shared.dto.PremiumScheduleDto;

import java.util.List;
import java.util.Optional;

public interface PolicyService {
    Optional<PolicyDto> getPolicyById(String id);

    List<PolicyDto> getAllPolicies();

    PolicyDto createPolicy(PolicyDto policyDto);

    void deletePolicy(String id);

    List<PolicyDto> getPoliciesForCustomer(String customerId);

    Optional<PremiumScheduleDto> getPremiumScheduleForPolicy(String id);
}