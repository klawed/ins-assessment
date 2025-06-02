package com.billing.policy.mapper;

import com.billing.policy.entity.Policy;
import com.billing.shared.dto.PolicyDto;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {
    
    public PolicyDto toDto(Policy entity) {
        return PolicyDto.builder()
                .policyId(entity.getId())
                .policyNumber(entity.getPolicyNumber())
                .customerId(entity.getCustomerId())
                .policyType(entity.getPolicyType())
                .status(mapStatus(entity.getStatus()))
                .effectiveDate(entity.getEffectiveDate())
                .expirationDate(entity.getExpirationDate())
                .premiumAmount(entity.getPremiumAmount())
                .frequency(entity.getFrequency())
                .gracePeriodDays(entity.getGracePeriodDays())
                .nextDueDate(entity.getNextDueDate())
                .build();
    }
    
    public Policy toEntity(PolicyDto dto) {
        return Policy.builder()
                .id(dto.getPolicyId())
                .policyNumber(dto.getPolicyNumber())
                .customerId(dto.getCustomerId())
                .policyType(dto.getPolicyType())
                .status(mapStatus(dto.getStatus()))
                .effectiveDate(dto.getEffectiveDate())
                .expirationDate(dto.getExpirationDate())
                .premiumAmount(dto.getPremiumAmount())
                .frequency(dto.getFrequency())
                .gracePeriodDays(dto.getGracePeriodDays())
                .nextDueDate(dto.getNextDueDate())
                .build();
    }
    
    private PolicyDto.PolicyStatus mapStatus(Policy.PolicyStatus status) {
        return PolicyDto.PolicyStatus.valueOf(status.name());
    }
    
    private Policy.PolicyStatus mapStatus(PolicyDto.PolicyStatus status) {
        return Policy.PolicyStatus.valueOf(status.name());
    }
}