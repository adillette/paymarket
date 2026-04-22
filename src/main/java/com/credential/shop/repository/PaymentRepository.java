package com.credential.shop.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.credential.shop.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment,String>{
  

  
}
