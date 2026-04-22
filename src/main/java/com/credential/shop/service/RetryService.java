package com.credential.shop.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.credential.shop.client.TossPaymentClient;
import com.credential.shop.domain.Order;
import com.credential.shop.domain.Payment;
import com.credential.shop.domain.PaymentState;
import com.credential.shop.domain.PaymentStatus;
import com.credential.shop.dto.request.PaymentConfirmRequest;
import com.credential.shop.dto.response.PaymentConfirmResponse;
import com.credential.shop.global.OrderNotFoundException;
import com.credential.shop.infra.RedisStateRepository;
import com.credential.shop.repository.OrderRepository;
import com.credential.shop.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryService {
  private final RedisStateRepository redisRepository;
  private final TossPaymentClient tossPaymentClient;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;

  private static final int MAX_RETRY = 3;
  
  public int retry(String requestId){
  
    PaymentState state= redisRepository.findById(requestId)
            .orElseThrow(()-> new IllegalStateException( 
              "Retry 대상 상태 없음 - 데이터 유실 가능 requestId=" + requestId));        
    //최대 재시도 초과
    if(state.getRetryCount()>=MAX_RETRY){
      log.warn("[RetryService] 최대 재시도 초과 requestId={}", state.getRequestId());
      save(state, PaymentStatus.FAILED, state.getRetryCount(), null);
      return 0;
    }

    //retrying 상태로 변경해서 다음 재시도 시간 설정
    int nextCount= state.getRetryCount()+1; //횟수 *1분
    LocalDateTime nextRetryAt= LocalDateTime.now().plusMinutes(nextCount);
    save(state, PaymentStatus.RETRYING, nextCount, nextRetryAt);

    try {
      Order order = orderRepository.findById(state.getOrderId())
        .orElseThrow(OrderNotFoundException::new);

      PaymentConfirmResponse tossResult= tossPaymentClient.confirm(
        state.getRequestId(), 
        state.getOrderId(),
        order.getTotalAmount());

        
        log.info("[RetryService] 재시도 성공 orderId={}, retryCount={}",
                    state.getOrderId(), nextCount);
        order.markAsPaid();
        paymentRepository.save(new Payment(
          tossResult.getPaymentKey(), 
          state.getOrderId(), 
          order.getTotalAmount()));
          save(state, PaymentStatus.PAID, nextCount, null);
          return 1;
    } catch (Exception e) {
      log.warn("[RetryService] 재시도 실패 orderId={}, retryCount={}",
                    state.getOrderId(), nextCount);
            // 다음 재시도는 Scheduler가 nextRetryAt 보고 결정
            return 0;
    }
  }
  private void save(PaymentState state, PaymentStatus status, int retryCount, LocalDateTime nextRetryAt){
    redisRepository.save(PaymentState.builder()
  .requestId(state.getRequestId())
  .orderId(state.getOrderId())
  .status(status)
    .retryCount(retryCount)
    .updatedAt(LocalDateTime.now())
    .nextRetryAt(nextRetryAt)
    .build());

  }



}
