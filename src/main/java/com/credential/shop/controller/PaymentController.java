package com.credential.shop.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.credential.shop.domain.Payment;
import com.credential.shop.dto.request.PaymentConfirmRequest;
import com.credential.shop.dto.response.PaymentConfirmResponse;
import com.credential.shop.service.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
  private final PaymentService paymentService;

  @PostMapping("/confirm")
  public ResponseEntity<PaymentConfirmResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {

    int result = paymentService.confirm(request);
    if (result == 1) {
      return ResponseEntity.ok(PaymentConfirmResponse.builder()
          .result(1)
          .message("결제성공")
          .build());

    } else {
      return ResponseEntity.status(400).body(PaymentConfirmResponse.builder()
          .result(0)
          .message("결제 실패")
          .build());
    }
  }

}
