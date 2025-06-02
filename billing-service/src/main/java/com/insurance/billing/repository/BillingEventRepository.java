package com.insurance.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.insurance.billing.entity.BillingEvent;
import com.insurance.billing.entity.BillingEvent.EventType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BillingEventRepository extends JpaRepository<BillingEvent, String> {
    List<BillingEvent> findByBillingId(String billingId);
    List<BillingEvent> findByPolicyId(String policyId);
    List<BillingEvent> findByEventType(EventType eventType);
    List<BillingEvent> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT be FROM BillingEvent be WHERE be.billingId = :billingId ORDER BY be.occurredAt DESC")
    List<BillingEvent> findEventHistoryForBilling(@Param("billingId") String billingId);
    
    @Query("SELECT be FROM BillingEvent be WHERE be.policyId = :policyId ORDER BY be.occurredAt DESC")
    List<BillingEvent> findEventHistoryForPolicy(@Param("policyId") String policyId);
    
    @Query("SELECT be FROM BillingEvent be WHERE be.eventType = :eventType AND be.occurredAt >= :since ORDER BY be.occurredAt DESC")
    List<BillingEvent> findRecentEventsByType(@Param("eventType") EventType eventType, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(be) FROM BillingEvent be WHERE be.eventType = :eventType AND be.occurredAt BETWEEN :start AND :end")
    Long countEventsByTypeInRange(@Param("eventType") EventType eventType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT be FROM BillingEvent be WHERE be.billingId IN :billingIds ORDER BY be.occurredAt DESC")
    List<BillingEvent> findEventsByBillingIds(@Param("billingIds") List<String> billingIds);
}