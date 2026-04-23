package com.credential.shop.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.credential.shop.client.TossPaymentClient;
import com.credential.shop.domain.Order;
import com.credential.shop.domain.Payment;
import com.credential.shop.domain.PaymentState;
import com.credential.shop.domain.PaymentStatus;
import com.credential.shop.dto.request.PaymentConfirmRequest;
import com.credential.shop.dto.response.PaymentConfirmResponse;
import com.credential.shop.global.OrderNotFoundException;
import com.credential.shop.global.TossPaymentException;
import com.credential.shop.infra.RedisStateRepository;
import com.credential.shop.repository.OrderRepository;
import com.credential.shop.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {
  private final RedisStateRepository redisRepository;
  private final TossPaymentClient tossPaymentClient;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;

  public PaymentConfirmResponse process(String orderId, PaymentConfirmRequest request){
    String requestId= request.getPaymentKey(); //toss에서 이 키로 결제 정보 확인함
   
    PaymentState state= PaymentState.builder()
                              .orderId(orderId) //스케줄러가 order 조회할때 필요
                              .requestId(requestId) //paymentKey -> redis pk
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


      //Order db 저장 누락
      Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
      order.markAsPaid();
      orderRepository.save(order);
      redisRepository.updateStatus(requestId,PaymentStatus.PAID);

      return response;

    }catch(Exception e){
      // 응답 유실 구분이 필요 
      //타임아웃/네트워크 끊김 -> 성공 여부 불명확 -> unknown

      redisRepository.updateStatus(requestId, PaymentStatus.UNKNOWN);
      throw new TossPaymentException();
    }
  }
}


/*
} catch (ResourceAccessException e) {
    // 타임아웃/네트워크 끊김 → 성공 여부 불명확 → UNKNOWN
    log.warn("[PaymentProcessor] 응답 유실 orderId={}", orderId);
    redisRepository.updateStatus(requestId, PaymentStatus.UNKNOWN);
    throw new TossPaymentUnknownException();

} catch (TossPaymentException e) {
    // Toss가 명확한 실패 응답 → FAILED
    log.warn("[PaymentProcessor] 결제 실패 orderId={}", orderId);
    redisRepository.updateStatus(requestId, PaymentStatus.FAILED);
    throw e;

} catch (DataAccessException e) {
    // DB 저장 실패 → Toss는 성공했으니 UNKNOWN
    log.error("[PaymentProcessor] DB 저장 실패 orderId={}", orderId);
    redisRepository.updateStatus(requestId, PaymentStatus.UNKNOWN);
    throw new TossPaymentUnknownException();
}


*/