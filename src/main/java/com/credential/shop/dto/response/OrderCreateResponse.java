package com.credential.shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderCreateResponse {
  private String orderId;
  private Integer totalAmount;
  private String status;
}
