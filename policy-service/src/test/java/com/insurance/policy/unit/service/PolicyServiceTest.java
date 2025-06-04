package com.insurance.policy.unit.service;

import com.insurance.policy.service.PolicyServiceImpl;
import com.insurance.policy.repository.PolicyRepository;
import com.insurance.policy.mapper.PolicyMapper;
import com.insurance.shared.dto.PolicyDto;
import com.insurance.shared.enums.PaymentFrequency;
import com.insurance.shared.enums.PolicyStatus;
import com.insurance.shared.dto.PremiumScheduleDto;
import com.insurance.policy.entity.PolicyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyMapper policyMapper;

    @InjectMocks
    private PolicyServiceImpl policyService; // Use the concrete implementation

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGetPolicyById() {
        PolicyEntity mockEntity = new PolicyEntity();
        mockEntity.setId("POLICY-123");
        when(policyRepository.findById("POLICY-123")).thenReturn(Optional.of(mockEntity));

        PolicyDto mockDto = PolicyDto.builder().id("POLICY-123").build();
        when(policyMapper.toDto(mockEntity)).thenReturn(mockDto);

        Optional<PolicyDto> result = policyService.getPolicyById("POLICY-123");

        assertTrue(result.isPresent());
        assertEquals("POLICY-123", result.get().getId());
        verify(policyRepository).findById("POLICY-123");
        verify(policyMapper).toDto(mockEntity);
    }

    @Test
    void shouldGetAllPolicies() {
        PolicyEntity mockEntity1 = new PolicyEntity();
        mockEntity1.setId("POLICY-001");
        PolicyEntity mockEntity2 = new PolicyEntity();
        mockEntity2.setId("POLICY-002");

        when(policyRepository.findAll()).thenReturn(List.of(mockEntity1, mockEntity2));

        PolicyDto mockDto1 = PolicyDto.builder().id("POLICY-001").build();
        PolicyDto mockDto2 = PolicyDto.builder().id("POLICY-002").build();
        when(policyMapper.toDto(mockEntity1)).thenReturn(mockDto1);
        when(policyMapper.toDto(mockEntity2)).thenReturn(mockDto2);

        List<PolicyDto> result = policyService.getAllPolicies();

        assertEquals(2, result.size());
        assertEquals("POLICY-001", result.get(0).getId());
        assertEquals("POLICY-002", result.get(1).getId());
        verify(policyRepository).findAll();
        verify(policyMapper).toDto(mockEntity1);
        verify(policyMapper).toDto(mockEntity2);
    }

    @Test
    void shouldCreatePolicy() {
        PolicyDto mockDto = PolicyDto.builder().id("POLICY-123").build();
        PolicyEntity mockEntity = new PolicyEntity();
        when(policyMapper.toEntity(mockDto)).thenReturn(mockEntity);

        PolicyEntity savedEntity = new PolicyEntity();
        savedEntity.setId("POLICY-123");
        when(policyRepository.save(mockEntity)).thenReturn(savedEntity);

        when(policyMapper.toDto(savedEntity)).thenReturn(mockDto);

        PolicyDto result = policyService.createPolicy(mockDto);

        assertEquals("POLICY-123", result.getId());
        verify(policyMapper).toEntity(mockDto);
        verify(policyRepository).save(mockEntity);
        verify(policyMapper).toDto(savedEntity);
    }

    @Test
    void shouldDeletePolicy() {
        String policyId = "POLICY-123";

        doNothing().when(policyRepository).deleteById(policyId);

        policyService.deletePolicy(policyId);

        verify(policyRepository).deleteById(policyId);
    }

    @Test
    void shouldGetPoliciesForCustomer() {
        String customerId = "CUST-001";

        PolicyEntity mockEntity1 = new PolicyEntity();
        mockEntity1.setId("POLICY-001");
        PolicyEntity mockEntity2 = new PolicyEntity();
        mockEntity2.setId("POLICY-002");

        when(policyRepository.findByCustomerId(customerId)).thenReturn(List.of(mockEntity1, mockEntity2));

        PolicyDto mockDto1 = PolicyDto.builder().id("POLICY-001").build();
        PolicyDto mockDto2 = PolicyDto.builder().id("POLICY-002").build();
        when(policyMapper.toDto(mockEntity1)).thenReturn(mockDto1);
        when(policyMapper.toDto(mockEntity2)).thenReturn(mockDto2);

        List<PolicyDto> result = policyService.getPoliciesForCustomer(customerId);

        assertEquals(2, result.size());
        assertEquals("POLICY-001", result.get(0).getId());
        assertEquals("POLICY-002", result.get(1).getId());
        verify(policyRepository).findByCustomerId(customerId);
        verify(policyMapper).toDto(mockEntity1);
        verify(policyMapper).toDto(mockEntity2);
    }

    @Test
    void shouldGetPremiumScheduleForPolicy() {
        String policyId = "POLICY-123";

        PolicyEntity mockEntity = new PolicyEntity();
        mockEntity.setId(policyId);
        mockEntity.setPremiumAmount(new BigDecimal("200.00"));
        mockEntity.setFrequency(PaymentFrequency.MONTHLY);
        mockEntity.setNextDueDate(LocalDate.now().plusDays(10));
        mockEntity.setGracePeriodDays(15);
        mockEntity.setStatus(PolicyStatus.ACTIVE);

        when(policyRepository.findById(policyId)).thenReturn(Optional.of(mockEntity));

        PremiumScheduleDto mockSchedule = PremiumScheduleDto.builder()
                .policyId(policyId)
                .premiumAmount(new BigDecimal("200.00"))
                .billingFrequency("MONTHLY")
                .nextDueDate(LocalDate.now().plusDays(10))
                .gracePeriodDays(15)
                .status("ACTIVE")
                .daysOverdue(null)
                .lateFee(null)
                .totalAmountDue(null)
                .schedule(List.of())
                .build();

        when(policyMapper.toPremiumScheduleDto(mockEntity)).thenReturn(mockSchedule);

        Optional<PremiumScheduleDto> result = policyService.getPremiumScheduleForPolicy(policyId);

        assertTrue(result.isPresent());
        assertEquals(policyId, result.get().getPolicyId());
        verify(policyRepository).findById(policyId);
        verify(policyMapper).toPremiumScheduleDto(mockEntity); // Ensure this is verified
    }
}