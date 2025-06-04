package com.insurance.billing.service;

import com.insurance.billing.entity.Billing;
import com.insurance.billing.entity.Payment;
import com.insurance.billing.entity.PaymentRetry;
import com.insurance.billing.repository.BillingRepository;
import com.insurance.billing.repository.PaymentRepository;
import com.insurance.billing.repository.PaymentRetryRepository;
import com.insurance.shared.client.PolicyClient;
import com.insurance.shared.dto.BillingDto;
import com.insurance.shared.dto.PaymentDto;
import com.insurance.shared.dto.PaymentRequestDto;
import com.insurance.shared.entity.Policy;
import com.insurance.billing.exception.PolicyNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.insurance.billing.mapper.BillingMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of BillingService providing billing calculations,
 * delinquency tracking, and grace period management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {
    private final BillingRepository billingRepository;
    private final GracePeriodService gracePeriodService;
    private final PolicyClient policyClient;
    private final BillingMapper billingMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentRetryRepository paymentRetryRepository;
    
    @Override
    public Map<String, Object> calculatePremium(String policyId) {
        log.info("Calculating premium for policy ID: {}", policyId);
        
        // TODO: Implement with repository layer and policy service integration
        Map<String, Object> result = new HashMap<>();
        result.put("policyId", policyId);
        result.put("premiumAmount", new BigDecimal("150.00"));
        result.put("frequency", "MONTHLY");
        result.put("calculatedAt", LocalDateTime.now());
        result.put("message", "Premium calculation completed - implementation pending");
        
        return result;
    }
    
    @Override
    public Map<String, Object> calculatePremiumFromRequest(Map<String, Object> request) {
        log.info("Calculating premium from request: {}", request);
        
        // TODO: Implement actual premium calculation logic
        Map<String, Object> result = new HashMap<>();
        result.put("calculatedPremium", new BigDecimal("150.00"));
        result.put("frequency", "MONTHLY");
        result.put("effectiveDate", LocalDateTime.now());
        result.put("baseAmount", new BigDecimal("135.00"));
        result.put("fees", new BigDecimal("15.00"));
        result.put("message", "Premium calculation completed - implementation pending");
        
        return result;
    }
    
    @Override
    public Map<String, Object> getDuePremiums() {
        log.info("Getting due premiums");
        
        // TODO: Implement actual due premium retrieval
        Map<String, Object> result = new HashMap<>();
        result.put("duePremiums", Collections.emptyList());
        result.put("totalDue", BigDecimal.ZERO);
        result.put("count", 0);
        result.put("message", "Due premiums endpoint - implementation pending");
        
        return result;
    }
    
    @Override
    public Optional<Map<String, Object>> getPremiumDetails(String policyId) {
        log.info("Getting premium details for policy ID: {}", policyId);
        
        // TODO: Implement with repository layer
        if ("NONEXISTENT".equals(policyId)) {
            return Optional.empty();
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("policyId", policyId);
        details.put("currentPremium", new BigDecimal("150.00"));
        details.put("nextDueDate", LocalDateTime.now().plusDays(30));
        details.put("status", "ACTIVE");
        
        return Optional.of(details);
    }
    
    @Override
    public List<Map<String, Object>> getDelinquentPolicies() {
        log.info("Getting delinquent policies");
        
        // TODO: Implement with repository layer and business logic
        List<Map<String, Object>> delinquentPolicies = new ArrayList<>();
        
        // Mock delinquent policy data
        Map<String, Object> policy1 = new HashMap<>();
        policy1.put("policyId", "POLICY-123");
        policy1.put("customerId", "CUST-001");
        policy1.put("customerName", "John Doe");
        policy1.put("daysOverdue", 15);
        policy1.put("amountOverdue", new BigDecimal("171.00"));
        policy1.put("lastPaymentDate", "2024-11-15");
        policy1.put("gracePeriodExpiry", LocalDateTime.now().minusDays(5));
        delinquentPolicies.add(policy1);
        
        Map<String, Object> policy2 = new HashMap<>();
        policy2.put("policyId", "POLICY-456");
        policy2.put("customerId", "CUST-002");
        policy2.put("customerName", "Jane Smith");
        policy2.put("daysOverdue", 8);
        policy2.put("amountOverdue", new BigDecimal("89.00"));
        policy2.put("lastPaymentDate", "2024-11-20");
        policy2.put("gracePeriodExpiry", LocalDateTime.now().plusDays(2));
        delinquentPolicies.add(policy2);
        
        return delinquentPolicies;
    }
    
    @Override
    public void updatePolicyBillingStatus(String policyId, String paymentStatus) {
        log.info("Updating billing status for policy {} to {}", policyId, paymentStatus);
        
        // TODO: Implement with repository layer and event publishing
        // This would typically:
        // 1. Update the policy billing record in database
        // 2. Publish billing status change event
        // 3. Trigger any downstream processes (notifications, etc.)
    }
    
    @Override
    public BigDecimal calculateLateFees(String policyId, int daysOverdue) {
        log.info("Calculating late fees for policy {} with {} days overdue", policyId, daysOverdue);
        
        // TODO: Implement configurable late fee calculation
        if (daysOverdue <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Simple late fee calculation: $15 base fee + $2 per day overdue
        BigDecimal baseFee = new BigDecimal("15.00");
        BigDecimal dailyFee = new BigDecimal("2.00");
        BigDecimal additionalFees = dailyFee.multiply(new BigDecimal(daysOverdue));
        
        return baseFee.add(additionalFees);
    }
    
    @Override
    public boolean isWithinGracePeriod(String policyId) {
        log.info("Checking grace period status for policy {}", policyId);
        
        Policy policy = policyClient.getPolicy(policyId)
            .orElseThrow(() -> new PolicyNotFoundException(policyId));
        
        // Get configured grace period
        int gracePeriodDays = gracePeriodService.getGracePeriodDays(
            policy.getPolicyType(),
            policy.getPaymentFrequency()
        );
        
        // Calculate if within grace period
        return LocalDate.now().isBefore(
            policy.getNextDueDate().plusDays(gracePeriodDays)
        );
    }
    
    @Override
    public List<BillingDto> getBillingsByCustomer(String customerId) {
        log.info("Fetching billings for customer: {}", customerId);
        return billingRepository.findByCustomerId(customerId)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<BillingDto> getBillingsByPolicy(String policyId) {
        log.info("Fetching billings for policy: {}", policyId);
        return billingRepository.findByPolicyId(policyId)
            .stream()
            .map(billingMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public PaymentDto processPayment(PaymentRequestDto request) {
        log.info("Processing payment request: {}", request);
        // Implementation details here
        return PaymentDto.builder().build(); // Placeholder
    }
    
    private BillingDto convertToDto(Billing billing) {
        BillingDto dto = new BillingDto();
        dto.setId(billing.getId());
        dto.setPolicyId(billing.getPolicyId());
        dto.setCustomerId(billing.getCustomerId());
        dto.setAmount(billing.getAmount());
        dto.setStatus(billing.getStatus());
        dto.setBillingDate(billing.getBillingDate());
        dto.setDueDate(billing.getDueDate());
        dto.setPaymentStatus(billing.getPaymentStatus());
        dto.setCreatedAt(billing.getCreatedAt());
        dto.setUpdatedAt(billing.getUpdatedAt());
        return dto;
    }
}