package com.credential.shop.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_retry_state")
public class PaymentRetryState {

  @Id
  private String requestId;
  private String orderId;
@Enumerated(EnumType.STRING)
  private PaymentStatus status;

  private int retryCount;
  private LocalDateTime updatedAt;
  private LocalDateTime nextRetryAt;  

  @Version
  private Long version;

  public PaymentRetryState(String requestId, String orderId, PaymentStatus status, int retryCount) {
    this.requestId = requestId;
    this.orderId = orderId;
    this.status = status;
    this.retryCount = retryCount;
    this.updatedAt = LocalDateTime.now();
  }

  public void markProcessing() {
    this.status = PaymentStatus.PROCESSING;
    this.updatedAt = LocalDateTime.now();
  }

  public void markUnknown() {
    this.status = PaymentStatus.UNKNOWN;
    this.updatedAt = LocalDateTime.now();
  }

  public void markPaid() {
    this.status = PaymentStatus.PAID;
    this.updatedAt = LocalDateTime.now();
  }

  public void markFailed() {
    this.status = PaymentStatus.FAILED;
    this.updatedAt = LocalDateTime.now();
  }

  public void markRetrying(int nextRetryCount, LocalDateTime nextRetryAt) {
    this.status = PaymentStatus.RETRYING;
    this.retryCount = nextRetryCount;
    this.nextRetryAt = nextRetryAt;
    this.updatedAt = LocalDateTime.now();
  }
  
}
