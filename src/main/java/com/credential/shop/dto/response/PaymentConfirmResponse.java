package com.credential.shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentConfirmResponse {
  private String paymentKey;
  private String orderId;
  private String status;
  private Integer totalAmount;
  private String approvedAt;
}
