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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
  private final PaymentService paymentService;

  @PostMapping("/confirm")
  public ResponseEntity<PaymentConfirmResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {

   PaymentConfirmResponse response= paymentService.confirm(request);
   return ResponseEntity.ok(response);
  }

  @GetMapping("/toss-success")
  public ResponseEntity<String> tossSuccess(
        @RequestParam String paymentKey,
        @RequestParam String orderId,
        @RequestParam int amount) {
      
        PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey,orderId,amount);
         PaymentConfirmResponse response = paymentService.confirm(request);
       return ResponseEntity.ok("결제 완료: " + response.toString());
  }
  

}
