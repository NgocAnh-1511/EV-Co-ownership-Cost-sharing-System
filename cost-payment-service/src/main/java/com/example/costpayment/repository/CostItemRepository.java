package com.example.costpayment.repository;

import com.example.costpayment.entity.CostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CostItemRepository extends JpaRepository<CostItem, Long> {
    
    List<CostItem> findByGroupId(String groupId);
    
    List<CostItem> findByVehicleId(String vehicleId);
    
    List<CostItem> findByGroupIdAndStatus(String groupId, CostItem.CostStatus status);
    
    List<CostItem> findByIncurredDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT c FROM CostItem c WHERE c.groupId = :groupId AND c.incurredDate BETWEEN :startDate AND :endDate")
    List<CostItem> findByGroupIdAndDateRange(@Param("groupId") String groupId, 
                                           @Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(c.totalAmount) FROM CostItem c WHERE c.groupId = :groupId AND c.status = 'PAID'")
    Double getTotalPaidAmount(@Param("groupId") String groupId);
    
    @Query("SELECT SUM(c.totalAmount - c.paidAmount) FROM CostItem c WHERE c.groupId = :groupId AND c.status IN ('PENDING', 'APPROVED')")
    Double getTotalOutstandingAmount(@Param("groupId") String groupId);
}
