package com.credential.shop.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.credential.shop.client.TossPaymentClient;
import com.credential.shop.domain.Order;
import com.credential.shop.domain.Payment;
import com.credential.shop.domain.PaymentRetryState;


import com.credential.shop.dto.response.PaymentConfirmResponse;
import com.credential.shop.global.OrderNotFoundException;

import com.credential.shop.repository.OrderRepository;
import com.credential.shop.repository.PaymentRepository;
import com.credential.shop.repository.PaymentRetryStateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryService {
   private final PaymentRetryStateRepository paymentRetryStateRepository;
  private final PaymentRetryStateWriter paymentRetryStateWriter;
  private final RetryLockService retryLockService;
  
  private final TossPaymentClient tossPaymentClient;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;

  private static final int MAX_RETRY = 3;
  
  public int retry(String requestId){
  
    if(!retryLockService.tryLock(requestId)){
      log.info("[RetryService] 이미 처리중이라 스킵");
      return 0;
    }


     try {
      PaymentRetryState state = paymentRetryStateRepository.findById(requestId)
          .orElseThrow(() -> new IllegalStateException(
              "Retry 대상 상태 없음 - 데이터 유실 가능 requestId=" + requestId));

      if (state.getRetryCount() >= MAX_RETRY) {
        log.warn("[RetryService] 최대 재시도 초과 requestId={}", requestId);
        paymentRetryStateWriter.markFailed(requestId);
        return 0;
      }

      int nextCount = state.getRetryCount() + 1;

      try {
        Order order = orderRepository.findById(state.getOrderId())
            .orElseThrow(OrderNotFoundException::new);

        PaymentConfirmResponse tossResult = tossPaymentClient.confirm(
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
        paymentRetryStateWriter.markPaid(requestId);
        return 1;

      } catch (Exception e) {
        log.warn("[RetryService] 재시도 실패 orderId={}, retryCount={}",
            state.getOrderId(), nextCount);
        LocalDateTime nextRetryAt = LocalDateTime.now().plusMinutes(nextCount);
        paymentRetryStateWriter.markRetrying(requestId, nextCount, nextRetryAt);
        return 0;
      }

    } finally {
      retryLockService.unlock(requestId);
    }
  }


}
