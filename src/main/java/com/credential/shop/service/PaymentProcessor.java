package com.credential.shop.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.credential.shop.client.TossPaymentClient;
import com.credential.shop.domain.Payment;
import com.credential.shop.domain.PaymentState;
import com.credential.shop.domain.PaymentStatus;
import com.credential.shop.dto.request.PaymentConfirmRequest;
import com.credential.shop.dto.response.PaymentConfirmResponse;
import com.credential.shop.global.TossPaymentException;
import com.credential.shop.infra.RedisStateRepository;
import com.credential.shop.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {
  private final RedisStateRepository redisRepository;
  private final TossPaymentClient tossPaymentClient;
  private final PaymentRepository paymentRepository;

  public PaymentConfirmResponse process(String orderId, PaymentConfirmRequest request){
    String requestId= request.getPaymentKey(); 
   
    PaymentState state= PaymentState.builder()
                              .orderId(orderId)
                              .requestId(requestId)
                              .status(PaymentStatus.PROCESSING)
                              .retryCount(0)
                              .updatedAt(LocalDateTime.now())
                              .build();
    redisRepository.save(state);

    try{
      //toss에서 들어오는 응답
      PaymentConfirmResponse response = tossPaymentClient.confirm(
              request.getPaymentKey(), 
              request.getOrderId(),
              request.getAmount());


      //성공시 db확정 
      Payment payment = new Payment(response.getPaymentKey(),orderId,response.getTotalAmount());//orderId 외부값 말고 내부값 쓰자
      payment.approve();
      paymentRepository.save(payment);//db 저장 성공
      redisRepository.updateStatus(requestId,PaymentStatus.PAID);

      return response;

    }catch(Exception e){
      // 응답 유실
      redisRepository.updateStatus(requestId, PaymentStatus.UNKNOWN);
      throw new TossPaymentException();
    }
  }
}
