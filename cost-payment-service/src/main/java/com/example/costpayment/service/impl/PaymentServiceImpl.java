package com.example.costpayment.service.impl;

import com.example.costpayment.entity.Payment;
import com.example.costpayment.entity.PaymentStatus;
import com.example.costpayment.repository.PaymentRepository;
import com.example.costpayment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> getPaymentById(Integer id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Payment createPayment(Payment payment) {
        // Generate transaction code if not provided
        if (payment.getTransactionCode() == null || payment.getTransactionCode().isEmpty()) {
            payment.setTransactionCode(generateTransactionCode());
        }
        
        // Set default status if not provided
        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING);
        }
        
        // Set payment date
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> updatePaymentStatus(Integer paymentId, String status) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            
            try {
                PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());
                payment.setStatus(newStatus);
                
                // Update payment date if status changed to PAID
                if (newStatus == PaymentStatus.PAID) {
                    payment.setPaymentDate(LocalDateTime.now());
                }
                
                return Optional.of(paymentRepository.save(payment));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid payment status: " + status);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Payment> getPaymentsByUserId(Integer userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public List<Payment> getPaymentsByCostId(Integer costId) {
        return paymentRepository.findByCostId(costId);
    }
    
    /**
     * Get pending payments by user ID
     */
    public List<Payment> getPendingPaymentsByUserId(Integer userId) {
        return paymentRepository.findPendingPaymentsByUserId(userId);
    }
    
    /**
     * Get payment history by user ID (completed payments)
     */
    public List<Payment> getPaymentHistoryByUserId(Integer userId) {
        return paymentRepository.findPaymentHistoryByUserId(userId);
    }
    
    /**
     * Generate unique transaction code
     */
    private String generateTransactionCode() {
        return "TXN" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

