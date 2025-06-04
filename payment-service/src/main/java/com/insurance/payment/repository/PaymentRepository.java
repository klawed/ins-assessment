package com.insurance.payment.repository;

import com.insurance.payment.entity.PaymentEntity;
import com.insurance.shared.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {

    List<PaymentEntity> findByPolicyId(String policyId);

    List<PaymentEntity> findByStatus(PaymentStatus status);
}