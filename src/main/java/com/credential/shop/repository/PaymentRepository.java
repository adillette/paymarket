package com.credential.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.credential.shop.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment,String>{
  boolean existsByPaymentKey(String paymentKeyString);
}
