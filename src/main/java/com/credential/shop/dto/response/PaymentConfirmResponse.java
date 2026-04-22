package com.credential.shop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentConfirmResponse {
  
  private String paymentKey;
  private String orderId;
  private String status;
  @JsonProperty
  private Integer totalAmount;
  @JsonProperty
  private String approvedAt;
}
