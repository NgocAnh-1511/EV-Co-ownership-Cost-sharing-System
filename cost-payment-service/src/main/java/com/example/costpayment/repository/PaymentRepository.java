package com.example.costpayment.repository;

import com.example.costpayment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    // Find payments by user ID
    List<Payment> findByUserIdOrderByPaymentDateDesc(Integer userId);
    
    // Find payments by cost ID
    List<Payment> findByCostIdOrderByPaymentDateDesc(Integer costId);
    
    // Find payments by status
    List<Payment> findByStatusOrderByPaymentDateDesc(Payment.PaymentStatus status);
    
    // Find payments by payment method
    List<Payment> findByMethodOrderByPaymentDateDesc(Payment.PaymentMethod method);
    
    // Find payments by user ID and status
    List<Payment> findByUserIdAndStatusOrderByPaymentDateDesc(Integer userId, Payment.PaymentStatus status);
    
    // Find payments by cost ID and status
    List<Payment> findByCostIdAndStatusOrderByPaymentDateDesc(Integer costId, Payment.PaymentStatus status);
    
    // Find payments by user ID and cost ID
    List<Payment> findByUserIdAndCostIdOrderByPaymentDateDesc(Integer userId, Integer costId);
    
    // Find payments by transaction code
    Payment findByTransactionCode(String transactionCode);
    
    // Find payments by date range
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentDateBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    // Find payments by user ID and date range
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<Payment> findByUserIdAndPaymentDateBetween(@Param("userId") Integer userId,
                                                    @Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    // Calculate total payment amount by user ID
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.userId = :userId")
    Double getTotalPaymentAmountByUserId(@Param("userId") Integer userId);
    
    // Calculate total payment amount by cost ID
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.costId = :costId")
    Double getTotalPaymentAmountByCostId(@Param("costId") Integer costId);
    
    // Calculate total payment amount by status
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    Double getTotalPaymentAmountByStatus(@Param("status") Payment.PaymentStatus status);
    
    // Calculate total payment amount by user ID and status
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    Double getTotalPaymentAmountByUserIdAndStatus(@Param("userId") Integer userId, 
                                                 @Param("status") Payment.PaymentStatus status);
    
    // Get payment summary by status
    @Query("SELECT p.status, COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p GROUP BY p.status")
    List<Object[]> getPaymentSummaryByStatus();
    
    // Get payment summary by method
    @Query("SELECT p.method, COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p GROUP BY p.method")
    List<Object[]> getPaymentSummaryByMethod();
    
    // Get payment summary by user
    @Query("SELECT p.userId, COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p GROUP BY p.userId ORDER BY SUM(p.amount) DESC")
    List<Object[]> getPaymentSummaryByUser();
    
    // Count payments by user ID
    Long countByUserId(Integer userId);
    
    // Count payments by cost ID
    Long countByCostId(Integer costId);
    
    // Count payments by status
    Long countByStatus(Payment.PaymentStatus status);
    
    // Count payments by method
    Long countByMethod(Payment.PaymentMethod method);
    
    // Check if payment exists by transaction code
    boolean existsByTransactionCode(String transactionCode);
    
    // Find pending payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'Pending' ORDER BY p.paymentDate ASC")
    List<Payment> findPendingPayments();
    
    // Find completed payments for a cost
    @Query("SELECT p FROM Payment p WHERE p.costId = :costId AND p.status = 'Completed' ORDER BY p.paymentDate DESC")
    List<Payment> findCompletedPaymentsForCost(@Param("costId") Integer costId);
}