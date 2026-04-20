package com.credential.shop.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelRequest {
  private String cancelReason;
}
