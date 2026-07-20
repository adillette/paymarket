package com.credential.shop.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.credential.shop.domain.PaymentRetryState;

import io.lettuce.core.dynamic.annotation.Param;

public interface PaymentRetryStateRepository extends JpaRepository<PaymentRetryState,String> {

  @Query("""
    SELECT s FROM PaymentRetryState s
    WHERE (s.status='PROCESSING' AND s.updatedAt< :processingCutoff)
      OR (s.status IN ('RETRYING','UNKNOWN')  
      AND (s.nextRetryAt IS NULL OR s.nextRetryAt <= :now))    
    """)
  List<PaymentRetryState> findRetryCandidates(
    @Param("processingCutoff") LocalDateTime processingCutoff,
    @Param("now") LocalDateTime now);
}
