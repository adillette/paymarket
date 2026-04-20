package com.credential.shop.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderCreateResponse {
  private String orderId;
  private int totalAmount;
  private int quantity;
  private String status;

  public OrderCreateResponse(String orderId,int totalAmount,int quantity,String status){
    this.orderId=orderId;
    this.totalAmount=totalAmount;
    this.quantity=quantity;
    this.status=status;
  }
}
