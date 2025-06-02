package com.billing.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for billing operations including premium calculations,
 * delinquency tracking, and grace period management.
 */
public interface BillingService {
    
    /**
     * Calculate premium amount for a specific policy
     * @param policyId The policy identifier
     * @return Map containing premium calculation details
     */
    Map<String, Object> calculatePremium(String policyId);
    
    /**
     * Calculate premium based on policy data and risk factors
     * @param request Map containing policy data for calculation
     * @return Map containing calculated premium details
     */
    Map<String, Object> calculatePremiumFromRequest(Map<String, Object> request);
    
    /**
     * Get all due premiums across policies
     * @return Map containing due premium information
     */
    Map<String, Object> getDuePremiums();
    
    /**
     * Get premium details for a specific policy
     * @param policyId The policy identifier
     * @return Optional containing premium details if found
     */
    Optional<Map<String, Object>> getPremiumDetails(String policyId);
    
    /**
     * Get all delinquent policies that require attention
     * @return List of delinquent policy information
     */
    List<Map<String, Object>> getDelinquentPolicies();
    
    /**
     * Update policy billing status based on payment events
     * @param policyId The policy identifier
     * @param paymentStatus The payment status update
     */
    void updatePolicyBillingStatus(String policyId, String paymentStatus);
    
    /**
     * Calculate late fees for overdue policies
     * @param policyId The policy identifier
     * @param daysOverdue Number of days the payment is overdue
     * @return Late fee amount
     */
    BigDecimal calculateLateFees(String policyId, int daysOverdue);
    
    /**
     * Check if policy is within grace period
     * @param policyId The policy identifier
     * @return true if within grace period, false otherwise
     */
    boolean isWithinGracePeriod(String policyId);
}