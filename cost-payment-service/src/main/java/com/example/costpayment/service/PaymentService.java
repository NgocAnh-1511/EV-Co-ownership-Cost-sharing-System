package com.example.costpayment.service;

import com.example.costpayment.entity.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentService {
    List<Payment> getAllPayments();
    Optional<Payment> getPaymentById(Integer id);
    Payment createPayment(Payment payment);
    Optional<Payment> updatePaymentStatus(Integer paymentId, String status);
    List<Payment> getPaymentsByUserId(Integer userId);
    List<Payment> getPaymentsByCostId(Integer costId);
}
