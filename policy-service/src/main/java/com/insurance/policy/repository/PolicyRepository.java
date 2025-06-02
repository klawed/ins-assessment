package com.insurance.policy.repository;

import com.insurance.policy.entity.PolicyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends CrudRepository<PolicyEntity, String> {
    List<PolicyEntity> findByCustomerId(String customerId);
}