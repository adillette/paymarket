package com.credential.shop.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor 
@AllArgsConstructor 
public class PaymentConfirmResponse {
  
  private String paymentKey;
  private String orderId;
  private String status;
  
  private Integer totalAmount;
  
  private String approvedAt;
}
