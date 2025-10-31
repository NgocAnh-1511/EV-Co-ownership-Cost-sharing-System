package com.example.costpayment.service.impl;

import com.example.costpayment.entity.Payment;
import com.example.costpayment.entity.PaymentStatus;
import com.example.costpayment.repository.PaymentRepository;
import com.example.costpayment.service.PaymentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private EntityManager entityManager;

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
        // Use native query to handle invalid enum values gracefully
        // This prevents Hibernate from failing when it encounters "Completed" status
        try {
            // Use native query that converts "Completed" to "PAID" in SQL
            Query query = entityManager.createNativeQuery(
                "SELECT paymentId, userId, costId, amount, transactionCode, method, " +
                "CASE WHEN status = 'Completed' THEN 'PAID' WHEN status IS NULL THEN 'PENDING' ELSE status END as status, " +
                "paymentDate " +
                "FROM Payment WHERE userId = :userId");
            query.setParameter("userId", userId);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            List<Payment> payments = new ArrayList<>();
            for (Object[] row : results) {
                Payment payment = new Payment();
                payment.setPaymentId(((Number) row[0]).intValue());
                payment.setUserId(((Number) row[1]).intValue());
                if (row[2] != null) {
                    payment.setCostId(((Number) row[2]).intValue());
                }
                payment.setAmount(((Number) row[3]).doubleValue());
                if (row[4] != null) {
                    payment.setTransactionCode((String) row[4]);
                }
                if (row[5] != null) {
                    try {
                        payment.setMethod(Payment.Method.valueOf((String) row[5]));
                    } catch (Exception ex) {
                        payment.setMethod(Payment.Method.EWallet);
                    }
                }
                // Handle status - convert "Completed" to "PAID"
                if (row[6] != null) {
                    String statusStr = (String) row[6];
                    if ("Completed".equalsIgnoreCase(statusStr)) {
                        statusStr = "PAID";
                    }
                    try {
                        payment.setStatus(PaymentStatus.valueOf(statusStr));
                    } catch (Exception ex) {
                        payment.setStatus(PaymentStatus.PENDING);
                    }
                } else {
                    payment.setStatus(PaymentStatus.PENDING);
                }
                if (row[7] != null) {
                    if (row[7] instanceof java.sql.Timestamp) {
                        payment.setPaymentDate(((java.sql.Timestamp) row[7]).toLocalDateTime());
                    } else if (row[7] instanceof java.time.LocalDateTime) {
                        payment.setPaymentDate((java.time.LocalDateTime) row[7]);
                    }
                }
                payments.add(payment);
            }
            
            return payments;
        } catch (Exception ex) {
            System.err.println("Error loading payments for userId " + userId + ": " + ex.getMessage());
            ex.printStackTrace();
            return new ArrayList<>(); // Return empty list rather than failing
        }
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

