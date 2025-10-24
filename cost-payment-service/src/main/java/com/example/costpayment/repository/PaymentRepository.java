package com.example.costpayment.repository;

import com.example.costpayment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByUserId(Integer userId);
    List<Payment> findByCost_CostId(Integer costId);
    List<Payment> findByStatus(Payment.PaymentStatus status);
}