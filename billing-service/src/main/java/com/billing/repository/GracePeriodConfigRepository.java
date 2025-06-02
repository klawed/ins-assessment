package com.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.billing.entity.GracePeriodConfig;
import com.billing.shared.enums.PaymentFrequency;
import com.billing.shared.enums.CustomerTier;
import java.util.Optional;

@Repository
public interface GracePeriodConfigRepository extends JpaRepository<GracePeriodConfig, String> {
    Optional<GracePeriodConfig> findByPolicyTypeAndFrequency(String policyType, PaymentFrequency frequency);
    
    Optional<GracePeriodConfig> findByPolicyTypeAndFrequencyAndCustomerTier(
        String policyType, 
        PaymentFrequency frequency,
        CustomerTier customerTier
    );
}