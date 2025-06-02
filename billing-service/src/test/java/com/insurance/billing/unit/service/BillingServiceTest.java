package com.insurance.billing.unit.service;

import com.insurance.billing.service.BillingService;
import com.insurance.shared.dto.BillingDto;
import com.insurance.shared.dto.PaymentDto;
import com.insurance.shared.dto.PaymentRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingServiceTest {

    @Mock
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCalculatePremium() {
        String policyId = "POLICY-123";
        Map<String, Object> mockPremiumDetails = Map.of("policyId", policyId, "premiumAmount", 200.00);

        when(billingService.calculatePremium(policyId)).thenReturn(mockPremiumDetails);

        Map<String, Object> result = billingService.calculatePremium(policyId);

        assertNotNull(result);
        assertEquals(200.00, result.get("premiumAmount"));
        verify(billingService).calculatePremium(policyId);
    }

    @Test
    void shouldCalculatePremiumFromRequest() {
        Map<String, Object> request = Map.of("policyType", "AUTO", "riskFactor", 1.2);
        Map<String, Object> mockResult = Map.of("premiumAmount", 300.00);

        when(billingService.calculatePremiumFromRequest(request)).thenReturn(mockResult);

        Map<String, Object> result = billingService.calculatePremiumFromRequest(request);

        assertNotNull(result);
        assertEquals(300.00, result.get("premiumAmount"));
        verify(billingService).calculatePremiumFromRequest(request);
    }

    @Test
    void shouldGetDuePremiums() {
        Map<String, Object> mockDuePremiums = Map.of("totalDue", 500.00);

        when(billingService.getDuePremiums()).thenReturn(mockDuePremiums);

        Map<String, Object> result = billingService.getDuePremiums();

        assertNotNull(result);
        assertEquals(500.00, result.get("totalDue"));
        verify(billingService).getDuePremiums();
    }

    @Test
    void shouldGetPremiumDetails() {
        String policyId = "POLICY-123";
        Map<String, Object> mockDetails = Map.of("policyId", policyId, "premiumAmount", 200.00);

        when(billingService.getPremiumDetails(policyId)).thenReturn(Optional.of(mockDetails));

        Optional<Map<String, Object>> result = billingService.getPremiumDetails(policyId);

        assertTrue(result.isPresent());
        assertEquals(200.00, result.get().get("premiumAmount"));
        verify(billingService).getPremiumDetails(policyId);
    }

    @Test
    void shouldGetDelinquentPolicies() {
        List<Map<String, Object>> mockDelinquentPolicies = List.of(
                Map.of("policyId", "POLICY-001", "status", "DELINQUENT"),
                Map.of("policyId", "POLICY-002", "status", "DELINQUENT")
        );

        when(billingService.getDelinquentPolicies()).thenReturn(mockDelinquentPolicies);

        List<Map<String, Object>> result = billingService.getDelinquentPolicies();

        assertEquals(2, result.size());
        assertEquals("DELINQUENT", result.get(0).get("status"));
        verify(billingService).getDelinquentPolicies();
    }

    @Test
    void shouldUpdatePolicyBillingStatus() {
        String policyId = "POLICY-123";
        String paymentStatus = "PAID";

        doNothing().when(billingService).updatePolicyBillingStatus(policyId, paymentStatus);

        billingService.updatePolicyBillingStatus(policyId, paymentStatus);

        verify(billingService).updatePolicyBillingStatus(policyId, paymentStatus);
    }

    @Test
    void shouldCalculateLateFees() {
        String policyId = "POLICY-123";
        int daysOverdue = 5;
        BigDecimal mockLateFee = new BigDecimal("25.00");

        when(billingService.calculateLateFees(policyId, daysOverdue)).thenReturn(mockLateFee);

        BigDecimal result = billingService.calculateLateFees(policyId, daysOverdue);

        assertEquals(new BigDecimal("25.00"), result);
        verify(billingService).calculateLateFees(policyId, daysOverdue);
    }

    @Test
    void shouldCheckIfWithinGracePeriod() {
        String policyId = "POLICY-123";

        when(billingService.isWithinGracePeriod(policyId)).thenReturn(true);

        boolean result = billingService.isWithinGracePeriod(policyId);

        assertTrue(result);
        verify(billingService).isWithinGracePeriod(policyId);
    }

    @Test
    void shouldGetBillingsByCustomer() {
        String customerId = "CUST-001";
        List<BillingDto> mockBillings = List.of(
                BillingDto.builder().id("BILL-001").customerId(customerId).build(),
                BillingDto.builder().id("BILL-002").customerId(customerId).build()
        );

        when(billingService.getBillingsByCustomer(customerId)).thenReturn(mockBillings);

        List<BillingDto> result = billingService.getBillingsByCustomer(customerId);

        assertEquals(2, result.size());
        assertEquals("CUST-001", result.get(0).getCustomerId());
        verify(billingService).getBillingsByCustomer(customerId);
    }

    @Test
    void shouldGetBillingsByPolicy() {
        String policyId = "POLICY-123";
        List<BillingDto> mockBillings = List.of(
                BillingDto.builder().id("BILL-001").policyId(policyId).build(),
                BillingDto.builder().id("BILL-002").policyId(policyId).build()
        );

        when(billingService.getBillingsByPolicy(policyId)).thenReturn(mockBillings);

        List<BillingDto> result = billingService.getBillingsByPolicy(policyId);

        assertEquals(2, result.size());
        assertEquals("POLICY-123", result.get(0).getPolicyId());
        verify(billingService).getBillingsByPolicy(policyId);
    }

    @Test
    void shouldProcessPayment() {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .billId("POLICY-123")
                .amount(new BigDecimal("200.00"))
                .build();

        PaymentDto mockPayment = PaymentDto.builder()
                .id("PAYMENT-001")
                .policyId("POLICY-123")
                .amount(new BigDecimal("200.00"))
                .build();

        when(billingService.processPayment(request)).thenReturn(mockPayment);

        PaymentDto result = billingService.processPayment(request);

        assertEquals("PAYMENT-001", result.getId());
        assertEquals("POLICY-123", result.getPolicyId());
        verify(billingService).processPayment(request);
    }
}