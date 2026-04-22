package com.credential.shop.domain;

import java.time.LocalDateTime;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {
  @Id
  private String paymentKey;
  private String orderId;
  private int amount;
  @Enumerated(EnumType.STRING)
  private PaymentStatus status;
  private LocalDateTime approvedAt;

  public Payment(String paymentKey, String orderId, int amount) {
    this.paymentKey = paymentKey;
    this.orderId = orderId;
    this.amount = amount;
   this.status = PaymentStatus.READY;
  }

  public void approve() {
    this.status = PaymentStatus.PAID;
    this.approvedAt = LocalDateTime.now();
}
}
