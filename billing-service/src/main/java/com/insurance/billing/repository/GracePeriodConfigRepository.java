package com.insurance.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.shared.enums.PaymentFrequency;
import com.insurance.billing.entity.GracePeriodConfig;
import com.insurance.shared.enums.CustomerTier;
import java.util.Optional;

@Repository
public interface GracePeriodConfigRepository extends JpaRepository<GracePeriodConfig, String> {
    Optional<GracePeriodConfig> findByPolicyTypeAndPaymentFrequency(String policyType, PaymentFrequency frequency);
    
    Optional<GracePeriodConfig> findByPolicyTypeAndPaymentFrequencyAndCustomerTier(
        String policyType, 
        PaymentFrequency paymentFrequency,
        CustomerTier customerTier
    );
}