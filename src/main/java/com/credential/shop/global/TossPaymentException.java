package com.credential.shop.global;

public class TossPaymentException extends RuntimeException{

  public TossPaymentException(){
    super();
  }

  public TossPaymentException(String message){
    super(message);
  }
}
