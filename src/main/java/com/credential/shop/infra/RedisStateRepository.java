package com.credential.shop.infra;

import java.time.LocalDateTime;

import org.springframework.data.repository.CrudRepository;

import com.credential.shop.domain.PaymentState;
import com.credential.shop.domain.PaymentStatus;

public interface RedisStateRepository extends CrudRepository<PaymentState,String>{

  default void updateStatus(String requestId, PaymentStatus status){
    PaymentState state= findById(requestId)
       .orElseThrow(()->new IllegalStateException("state 없음"));
    state.setStatus(status);
    state.setUpdatedAt(LocalDateTime.now());
    save(state);
  };

  
} 