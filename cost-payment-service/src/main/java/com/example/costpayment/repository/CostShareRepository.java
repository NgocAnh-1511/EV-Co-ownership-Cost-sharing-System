package com.example.costpayment.repository;

import com.example.costpayment.entity.CostShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostShareRepository extends JpaRepository<CostShare, Integer> {
    
    // Find shares by cost ID
    List<CostShare> findByCostIdOrderByCalculatedAtDesc(Integer costId);
    
    // Find shares by user ID
    List<CostShare> findByUserIdOrderByCalculatedAtDesc(Integer userId);
    
    // Find shares by cost ID and user ID
    CostShare findByCostIdAndUserId(Integer costId, Integer userId);
    
    // Find all shares for a specific cost
    @Query("SELECT cs FROM CostShare cs WHERE cs.costId = :costId ORDER BY cs.calculatedAt DESC")
    List<CostShare> findAllSharesForCost(@Param("costId") Integer costId);
    
    // Calculate total share amount for a cost
    @Query("SELECT COALESCE(SUM(cs.amountShare), 0) FROM CostShare cs WHERE cs.costId = :costId")
    Double getTotalShareAmountForCost(@Param("costId") Integer costId);
    
    // Calculate total share amount for a user
    @Query("SELECT COALESCE(SUM(cs.amountShare), 0) FROM CostShare cs WHERE cs.userId = :userId")
    Double getTotalShareAmountForUser(@Param("userId") Integer userId);
    
    // Get share summary by cost
    @Query("SELECT cs.userId, cs.percent, cs.amountShare FROM CostShare cs WHERE cs.costId = :costId ORDER BY cs.amountShare DESC")
    List<Object[]> getShareSummaryForCost(@Param("costId") Integer costId);
    
    // Check if user has share for cost
    boolean existsByCostIdAndUserId(Integer costId, Integer userId);
    
    // Count shares for a cost
    Long countByCostId(Integer costId);
    
    // Count shares for a user
    Long countByUserId(Integer userId);
    
    // Delete shares by cost ID
    void deleteByCostId(Integer costId);
    
    // Delete shares by user ID
    void deleteByUserId(Integer userId);
}