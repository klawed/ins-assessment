package com.billing.repository;

import com.billing.entity.PaymentRetry;
import com.billing.entity.PaymentRetry.RetryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository 
public interface PaymentRetryRepository extends JpaRepository<PaymentRetry, String> {
    List<PaymentRetry> findByPaymentId(String paymentId);
    List<PaymentRetry> findByBillingId(String billingId);
    List<PaymentRetry> findByStatus(RetryStatus status);
    
    @Query("SELECT pr FROM PaymentRetry pr WHERE pr.status = 'SCHEDULED' AND pr.scheduledAt <= :now ORDER BY pr.scheduledAt ASC")
    List<PaymentRetry> findDueRetries(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(pr) FROM PaymentRetry pr WHERE pr.billingId = :billingId")
    Integer countRetriesForBilling(@Param("billingId") String billingId);
    
    @Query("SELECT pr FROM PaymentRetry pr WHERE pr.paymentId = :paymentId ORDER BY pr.retryAttempt DESC")
    List<PaymentRetry> findRetriesByPaymentOrderByAttempt(@Param("paymentId") String paymentId);
    
    @Query("SELECT pr FROM PaymentRetry pr WHERE pr.status = 'FAILED' AND pr.retryAttempt < :maxRetries")
    List<PaymentRetry> findFailedRetriesEligibleForRetry(@Param("maxRetries") Integer maxRetries);
    
    @Query("SELECT MAX(pr.retryAttempt) FROM PaymentRetry pr WHERE pr.billingId = :billingId")
    Integer findMaxRetryAttemptForBilling(@Param("billingId") String billingId);
}