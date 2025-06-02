package com.billing.repository;

import com.billing.entity.Billing;
import com.billing.entity.Billing.BillingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillingRepository extends JpaRepository<Billing, String> {
    List<Billing> findByPolicyId(String policyId);
    List<Billing> findByCustomerId(String customerId);
    List<Billing> findByStatus(BillingStatus status);
    List<Billing> findByDueDateBefore(LocalDate date);
    List<Billing> findByStatusAndDueDateBefore(BillingStatus status, LocalDate date);
    
    @Query("SELECT b FROM Billing b WHERE b.status = 'OVERDUE' AND b.gracePeriodEnd < :date")
    List<Billing> findDelinquentBillings(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Billing b WHERE b.dueDate BETWEEN :start AND :end")
    List<Billing> findBillingsDueInRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
    
    @Query("SELECT b FROM Billing b WHERE b.status IN ('OVERDUE', 'GRACE_PERIOD') AND b.retryCount < :maxRetries")
    List<Billing> findBillingsEligibleForRetry(@Param("maxRetries") Integer maxRetries);
    
    @Query("SELECT COUNT(b) FROM Billing b WHERE b.customerId = :customerId AND b.status = 'DELINQUENT'")
    Long countDelinquentBillingsByCustomer(@Param("customerId") String customerId);
}