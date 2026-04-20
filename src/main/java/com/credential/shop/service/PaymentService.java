package com.credential.shop.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.credential.shop.client.TossPaymentClient;
import com.credential.shop.domain.Order;
import com.credential.shop.domain.Payment;
import com.credential.shop.dto.request.PaymentConfirmRequest;
import com.credential.shop.global.AmountMismatchException;
import com.credential.shop.global.DuplicatePaymentException;
import com.credential.shop.global.OrderNotFoundException;
import com.credential.shop.repository.OrderRepository;
import com.credential.shop.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final TossPaymentClient tossPaymentClient; 

  public int confirm(PaymentConfirmRequest request){
    //1.멱등성 체크
    if(paymentRepository.existsByPaymentKey(request.getPaymentKey())){
       throw new DuplicatePaymentException();
    }
    //금액 위변조 체크
    Order order = orderRepository.findById(request.getOrderId())
                  .orElseThrow(OrderNotFoundException::new);
    if(order.getTotalAmount()!=request.getAmount()){
      order.markAsFailed();
      throw new AmountMismatchException();
    }

    //toss api 호출
    Map<String, Object> tossResult = tossPaymentClient.confirm(
        request.getPaymentKey(),
        request.getOrderId(),
        request.getAmount()
    );
  
    String approvedAt = (String) tossResult.get("approvedAt");
    String status = (String) tossResult.get("status");
   
   log.info("[결제 성공] orderId={}, paymentKey={}, status={}, approvedAt={}",
                request.getOrderId(),
                request.getPaymentKey(),
                status,
                approvedAt);
   
    //상태 저장
    order.markAsPaid();
    paymentRepository.save(new Payment(
      request.getPaymentKey(),
      request.getOrderId() ,
      request.getAmount()
    ));
    return 1;
  }

}
