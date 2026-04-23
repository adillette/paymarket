package com.credential.shop.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.credential.shop.domain.Order;
import com.credential.shop.dto.request.PaymentConfirmRequest;
import com.credential.shop.dto.response.PaymentConfirmResponse;
import com.credential.shop.global.AmountMismatchException;
import com.credential.shop.global.DuplicatePaymentException;
import com.credential.shop.global.OrderNotFoundException;
import com.credential.shop.repository.OrderRepository;
import com.credential.shop.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/*
Redis done 체크-> 있으면 캐시 리턴
processing 체크 -> 있으면 409 리턴
setIfAbsent-> 선점 실패하면 409
금액 위변조 체크
toss api 호출 한번
db저장
redis done 캐싱
*/



@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

   
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  
  private final PaymentProcessor paymentProcessor;
  private static final String PREFIX = "payment:idem:";
  private static final long TTL = 3L;

  

  public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
    String key = PREFIX + request.getPaymentKey();

    //TTL 무관한 db 조회
    if(paymentRepository.existsByPaymentKey(request.getPaymentKey())){
      throw new DuplicatePaymentException();
    }
    // redis 상태 확인
    String status = redisTemplate.opsForValue().get(key);

    if (status != null && status.startsWith("DONE:")) {
      try {
        return objectMapper.readValue(status.substring(5), PaymentConfirmResponse.class);
      } catch (Exception e) {
        log.error("캐시 역직렬화 실패 key={}", key,e);
      }
    }
    //processing인지 체크
    if ("PROCESSING".equals(status)) {
      throw new DuplicatePaymentException();// 409에러
    }


    //원자적 선점
    Boolean isNew = redisTemplate.opsForValue()
        .setIfAbsent(key, "PROCESSING",
            TTL,
            TimeUnit.MINUTES);
    if (!Boolean.TRUE.equals(isNew)) {
      throw new DuplicatePaymentException();// 409
    }

    
      // 금액 위변조 체크
      Order order = orderRepository.findById(request.getOrderId())
          .orElseThrow(OrderNotFoundException::new);
      if (order.getTotalAmount() != request.getAmount()) {
        redisTemplate.delete(key);// 위변조는 재시도 없음
        order.markAsFailed();
        orderRepository.save(order);
        throw new AmountMismatchException();
      }
    //도입했고 실제 로직 payment Processor 로 이동 ㄱㄱ
  
  
   PaymentConfirmResponse response= paymentProcessor.process(request.getOrderId(), request);
      try {
        String resultJson=objectMapper.writeValueAsString(response);
        redisTemplate.opsForValue().set(key,"DONE:"+resultJson,TTL,TimeUnit.MINUTES);
      } catch (Exception e) {
        log.error("DONE 캐싱 실패 key={}", key, e);
      }
      return response;
  }

}
