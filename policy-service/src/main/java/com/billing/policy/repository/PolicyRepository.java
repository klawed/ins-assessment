package com.billing.policy.repository;

import com.billing.policy.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {
    List<Policy> findByCustomerId(String customerId);
    boolean existsByPolicyNumber(String policyNumber);
}