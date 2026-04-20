package com.credential.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.credential.shop.domain.Order;

public interface OrderRepository extends JpaRepository<Order,String>{

  
} 