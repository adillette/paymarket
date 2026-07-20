package com.credential.shop.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import com.credential.shop.client.TossPaymentClient;
import com.credential.shop.domain.Order;
import com.credential.shop.domain.Payment;

import com.credential.shop.domain.PaymentStatus;
import com.credential.shop.dto.request.PaymentConfirmRequest;
import com.credential.shop.dto.response.PaymentConfirmResponse;
import com.credential.shop.global.OrderNotFoundException;
import com.credential.shop.global.TossPaymentException;
import com.credential.shop.global.TossPaymentUnknownException;

import com.credential.shop.repository.OrderRepository;
import com.credential.shop.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessor {
   private final PaymentRetryStateWriter paymentRetryStateWriter;
  private final TossPaymentClient tossPaymentClient;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;

  public PaymentConfirmResponse process(String orderId, PaymentConfirmRequest request){
    String requestId= request.getPaymentKey(); //toss에서 이 키로 결제 정보 확인함
   
    paymentRetryStateWriter.saveInitial(requestId, orderId);

    try {
      PaymentConfirmResponse response = PaymentConfirmResponse.builder()
          .paymentKey(request.getPaymentKey())
          .orderId(request.getOrderId())
          .status("DONE")
          .totalAmount(request.getAmount())
          .approvedAt(LocalDateTime.now().toString())
          .build();

      Order order = orderRepository.findById(orderId)
          .orElseThrow(OrderNotFoundException::new);

      Payment payment = new Payment(response.getPaymentKey(), order.getOrderId(), response.getTotalAmount());
      payment.approve();
      paymentRepository.save(payment);

      order.markAsPaid();
      orderRepository.save(order);
      paymentRetryStateWriter.markPaid(requestId);

      return response;

    } catch (ResourceAccessException e) {
      log.warn("[PaymentProcessor] 응답 유실 orderId={}", orderId);
      paymentRetryStateWriter.markUnknown(requestId);
      throw new TossPaymentUnknownException();

    } catch (TossPaymentException e) {
      log.warn("[PaymentProcessor] 결제 실패 orderId={}", orderId);
      paymentRetryStateWriter.markFailed(requestId);
      throw e;

    } catch (DataAccessException e) {
      log.error("[PaymentProcessor] DB 저장 실패 orderId={}", orderId);
      paymentRetryStateWriter.markUnknown(requestId);
      throw new TossPaymentUnknownException();
    }
  }
  }





