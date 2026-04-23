package com.credential.shop.global;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(DuplicatePaymentException.class)
  public ResponseEntity<String> handleDuplicate() {
    return ResponseEntity.status(409).body("이미 처리된 결제입니다.");
  }

  @ExceptionHandler(AmountMismatchException.class)
  public ResponseEntity<String> handleAmountMismatch() {
    return ResponseEntity.status(400).body("결제 금액이 일치하지 않습니다.");
  }

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<String> handleOrderNotFound() {
    return ResponseEntity.status(400).body("주문이 존재하지 않습니다.");
  }

  @ExceptionHandler(TossPaymentException.class)
  public ResponseEntity<String> handleTossPayment() {
    return ResponseEntity.status(500).body("결제 처리 중 오류가 발생했습니다.");
  }

  @ExceptionHandler(TossPaymentUnknownException.class)
  public ResponseEntity<String> handleTossUnknown() {
      return ResponseEntity.status(202).body("결제 처리 중입니다. 잠시 후 확인해주세요.");
  }
}
