package com.insurance.policy.service;

import com.insurance.policy.entity.PolicyEntity;
import com.insurance.policy.mapper.PolicyMapper;
import com.insurance.policy.repository.PolicyRepository;
import com.insurance.shared.dto.PolicyDto;
import com.insurance.shared.dto.PremiumScheduleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;


    @Override
    public Optional<PolicyDto> getPolicyById(String id) {
        return policyRepository.findById(id).map(policyMapper::toDto);
    }

    @Override
    public List<PolicyDto> getAllPolicies() {
        return StreamSupport.stream(policyRepository.findAll().spliterator(), false)
                .map(policyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PolicyDto createPolicy(PolicyDto policyDto) {
        PolicyEntity entity = policyMapper.toEntity(policyDto);
        PolicyEntity savedEntity = policyRepository.save(entity);
        return policyMapper.toDto(savedEntity);
    }

    @Override
    public void deletePolicy(String id) {
        policyRepository.deleteById(id);
    }

    @Override
    public List<PolicyDto> getPoliciesForCustomer(String customerId) {
        return StreamSupport.stream(policyRepository.findByCustomerId(customerId).spliterator(), false)
                .map(policyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PremiumScheduleDto> getPremiumScheduleForPolicy(String id) {
        return policyRepository.findById(id)
                .map(policyMapper::toPremiumScheduleDto); // Ensure this method is called
    }
}