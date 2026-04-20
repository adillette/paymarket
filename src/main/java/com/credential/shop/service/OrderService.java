package com.credential.shop.service;

import org.springframework.stereotype.Service;

import com.credential.shop.domain.Order;
import com.credential.shop.dto.request.OrderCreateRequest;
import com.credential.shop.dto.response.OrderCreateResponse;
import com.credential.shop.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
  private OrderRepository orderRepository;

  public OrderCreateResponse createOrder(OrderCreateRequest request){
    Order order = new Order(
      request.getProductId(),
      request.getQuantity(),
      request.getTotalAmount());

    orderRepository.save(order);

    return new OrderCreateResponse(order.getOrderId(), order.getTotalAmount(),order.getStatus().name());
  }
}
