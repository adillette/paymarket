package com.credential.shop.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.credential.shop.dto.request.OrderCreateRequest;
import com.credential.shop.dto.response.OrderCreateResponse;
import com.credential.shop.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  @PostMapping("/create")
  public ResponseEntity<OrderCreateResponse> createOrder(@RequestBody OrderCreateRequest request) {
      
    OrderCreateResponse response=orderService.createOrder(request);
      
    return ResponseEntity.ok(response);
  }
  
}
