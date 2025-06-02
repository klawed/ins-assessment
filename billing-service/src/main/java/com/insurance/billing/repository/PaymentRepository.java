package com.insurance.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.insurance.billing.entity.Payment;
import com.insurance.billing.entity.Payment.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByBillingId(String billingId);
    List<Payment> findByPolicyId(String policyId);
    List<Payment> findByCustomerId(String customerId);
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.attemptedAt BETWEEN :start AND :end ORDER BY p.attemptedAt DESC")
    List<Payment> findPaymentHistory(@Param("customerId") String customerId, 
                                   @Param("start") LocalDateTime start, 
                                   @Param("end") LocalDateTime end);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.billingId IN :billingIds")
    List<Payment> findFailedPaymentsForBillings(@Param("billingIds") List<String> billingIds);
    
    @Query("SELECT p FROM Payment p WHERE p.billingId = :billingId AND p.status = 'SUCCESS' ORDER BY p.processedAt DESC")
    List<Payment> findSuccessfulPaymentsByBilling(@Param("billingId") String billingId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED' AND p.attemptedAt >= :since")
    Long countFailedPaymentsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.attemptedAt < :cutoff")
    List<Payment> findStalePayments(@Param("cutoff") LocalDateTime cutoff);
}