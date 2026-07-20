package com.credential.shop.service;



import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.credential.shop.domain.PaymentRetryState;
import com.credential.shop.domain.PaymentStatus;
import com.credential.shop.repository.PaymentRetryStateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentRetryStateWriter {
  private final PaymentRetryStateRepository paymentRetryStateRepository;

 @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveInitial(String requestId, String orderId) {
    paymentRetryStateRepository.save(
        new PaymentRetryState(requestId, orderId, PaymentStatus.PROCESSING, 0));
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markUnknown(String requestId) {
    PaymentRetryState state = paymentRetryStateRepository.findById(requestId)
        .orElseThrow(() -> new IllegalStateException("재시도 상태 없음 requestId=" + requestId));
    state.markUnknown();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markPaid(String requestId) {
    PaymentRetryState state = paymentRetryStateRepository.findById(requestId)
        .orElseThrow(() -> new IllegalStateException("재시도 상태 없음 requestId=" + requestId));
    state.markPaid();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markFailed(String requestId) {
    PaymentRetryState state = paymentRetryStateRepository.findById(requestId)
        .orElseThrow(() -> new IllegalStateException("재시도 상태 없음 requestId=" + requestId));
    state.markFailed();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markRetrying(String requestId, int nextRetryCount, LocalDateTime nextRetryAt) {
    PaymentRetryState state = paymentRetryStateRepository.findById(requestId)
        .orElseThrow(() -> new IllegalStateException("재시도 상태 없음 requestId=" + requestId));
    state.markRetrying(nextRetryCount, nextRetryAt);
  }
}
