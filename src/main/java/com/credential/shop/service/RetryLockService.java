package com.credential.shop.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetryLockService {
  private final StringRedisTemplate redisTemplate;
  private static final String LOCK_KEY_PREFIX = "payment:lock:";
  private static final Duration LOCK_TTL = Duration.ofSeconds(90);
    
  
  
  public boolean tryLock(String requestId) {
    Boolean acquired = redisTemplate.opsForValue()
        .setIfAbsent(LOCK_KEY_PREFIX + requestId, "1", LOCK_TTL.getSeconds(), TimeUnit.SECONDS);
    return Boolean.TRUE.equals(acquired);
  }

  public void unlock(String requestId) {
    redisTemplate.delete(LOCK_KEY_PREFIX + requestId);
  }
}
