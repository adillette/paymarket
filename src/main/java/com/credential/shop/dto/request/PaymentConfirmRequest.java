package com.credential.shop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {
  private String paymentKey;
  private String orderId;
  private Integer amount;
}
