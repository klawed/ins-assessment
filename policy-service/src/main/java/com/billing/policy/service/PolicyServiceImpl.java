package com.billing.policy.service;

import com.billing.policy.repository.PolicyRepository;
import com.billing.policy.mapper.PolicyMapper;
import com.billing.shared.dto.PolicyDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class PolicyServiceImpl implements PolicyService {
    
    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;
    
    public PolicyServiceImpl(PolicyRepository policyRepository, PolicyMapper policyMapper) {
        this.policyRepository = policyRepository;
        this.policyMapper = policyMapper;
    }
    
    @Override
    public Optional<PolicyDto> getPolicyById(String policyId) {
        return policyRepository.findById(policyId)
                .map(policyMapper::toDto);
    }
    
    @Override
    public List<PolicyDto> getPoliciesByCustomerId(String customerId) {
        return policyRepository.findByCustomerId(customerId).stream()
                .map(policyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPremiumSchedule(String policyId) {
        // TODO: Implement premium calculation logic
        return new HashMap<>();
    }
}