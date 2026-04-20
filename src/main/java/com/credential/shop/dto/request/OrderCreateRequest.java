package com.credential.shop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {
  private Long productId;
  private Integer quantity;
  private Integer amount;
}
