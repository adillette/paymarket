package com.credential.shop.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.credential.shop.domain.PaymentRetryState;

import com.credential.shop.domain.PaymentStatus;

import com.credential.shop.repository.PaymentRetryStateRepository;
import com.credential.shop.service.PaymentRetryStateWriter;
import com.credential.shop.service.RetryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSyncScheduler {

  private final PaymentRetryStateRepository paymentRetryStateRepository;
  private final PaymentRetryStateWriter paymentRetryStateWriter;
  private final RetryService retryService;

  @Scheduled(fixedDelay = 60000)
  public void sync() {

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime processingCutoff = now.minusMinutes(2);

    List<PaymentRetryState> candidates =
        paymentRetryStateRepository.findRetryCandidates(processingCutoff, now);


    for (PaymentRetryState  state : candidates) {
       if (state.getStatus() == PaymentStatus.PROCESSING) {
        log.warn("[Scheduler] PROCESSING 응답 유실 의심 → UNKNOWN 전환 requestId={}",
            state.getRequestId());
        paymentRetryStateWriter.markUnknown(state.getRequestId());
        continue;
      }
      int result = retryService.retry(state.getRequestId());
      log.info("[Scheduler] 재시도 결과 requestId={} result={}",
          state.getRequestId(), result == 1 ? "성공" : "실패");
    }
  }
}
