package com.credential.shop.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order {
  @Id
  private String orderId;
  private Long productId;
  private int quantity;
  private int totalAmount;
  
  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  private LocalDateTime createdAt;

  public Order(Long productId,int quantity,int totalAmount) {
    this.orderId = UUID.randomUUID().toString(); // 랜덤 고유 ID 자동 생성
    this.productId = productId; // 외부에서 받은 값 저장
    this.quantity=quantity;
    this.totalAmount = totalAmount; // 외부에서 받은 값 저장
  }

  public void markAsPaid(){
    this.status=OrderStatus.PAID;
  }
  public void markAsFailed(){
    this.status=OrderStatus.FAILED;

  }
}
