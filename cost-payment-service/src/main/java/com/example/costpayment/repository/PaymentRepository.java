package com.example.costpayment.repository;

import com.example.costpayment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByCostItemId(Long costItemId);
    
    List<Payment> findByUserId(String userId);
    
    List<Payment> findByUserIdAndStatus(String userId, Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.costItem.groupId = :groupId")
    List<Payment> findByGroupId(@Param("groupId") String groupId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    Double getTotalPaidByUser(@Param("userId") String userId);
}
