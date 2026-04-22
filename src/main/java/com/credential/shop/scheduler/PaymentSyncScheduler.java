package com.credential.shop.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.credential.shop.domain.PaymentState;
import com.credential.shop.domain.PaymentStatus;
import com.credential.shop.infra.RedisStateRepository;
import com.credential.shop.service.RetryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSyncScheduler {

  private final RedisStateRepository redisRepository;
  private final RetryService retryService;

  @Scheduled(fixedDelay = 60000)
  public void sync() {

    Iterable<PaymentState> all = redisRepository.findAll();

    for (PaymentState state : all) {
      switch (state.getStatus()) {

        case PROCESSING:
          // 5분 지났으면 응답 유실로 판단해서 unknown으로 변경한다.
          if (state.getUpdatedAt().isBefore(LocalDateTime.now()
              .minusMinutes(5))) {
            redisRepository.save(PaymentState.builder()
                .requestId(state.getRequestId())
                .orderId(state.getOrderId())
                .status(PaymentStatus.UNKNOWN)
                .retryCount(0)
                .updatedAt(LocalDateTime.now())
                .nextRetryAt(null)
                .build());
            log.warn("[Scheduler] PROCESSING 응답 유실 의심 → UNKNOWN 전환 requestId={}",
                state.getRequestId());
          }
          break;

        case RETRYING:
          if (state.getNextRetryAt() != null
              && LocalDateTime.now().isBefore(state.getNextRetryAt())) {
            log.info("[Scheduler] 대기 중 orderId={}",
                state.getOrderId());
            continue;
          }
          int retryResult = retryService.retry(state.getRequestId());
          log.info("[Scheduler] RETRYING 재시도 결과 requestId={} result={}",
              state.getRequestId(), retryResult == 1 ? "성공" : "실패");
          break;

        case UNKNOWN:
          if (state.getNextRetryAt() != null
              && LocalDateTime.now().isBefore(state.getNextRetryAt())) {
            log.info("[Scheduler] 대기 중 orderId={}", state.getOrderId());
            continue;
          }
          int result = retryService.retry(state.getRequestId());
          log.info("[Scheduler] UNKNOWN 재시도 결과 requestId={} result={}",
              state.getRequestId(), result == 1 ? "성공" : "실패");

          break;

      }
    }
  }
}
