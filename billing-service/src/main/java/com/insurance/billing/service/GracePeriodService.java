package com.insurance.billing.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import com.insurance.shared.enums.PaymentFrequency;
import com.insurance.billing.entity.GracePeriodConfig;
import com.insurance.billing.repository.GracePeriodConfigRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class GracePeriodService {
    private final GracePeriodConfigRepository gracePeriodConfigRepository;
    
    public int getGracePeriodDays(String policyType, PaymentFrequency frequency) {
        return gracePeriodConfigRepository
            .findByPolicyTypeAndFrequency(policyType, frequency)
            .or(() -> gracePeriodConfigRepository.findByPolicyTypeAndFrequency("DEFAULT", frequency))
            .map(GracePeriodConfig::getGracePeriodDays)
            .orElse(10); // Hardcoded fallback
    }
}