package com.credential.shop.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


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
@RedisHash(value="payment",timeToLive = 180)
public class PaymentState {

  @Id
  private String requestId;// idempotency key → Redis key
  private String orderId;
  private PaymentStatus status;
  private int retryCount;
  private LocalDateTime updatedAt;
  private LocalDateTime nextRetryAt;  
  
}
