package com.example.costpayment.repository;

import com.example.costpayment.entity.Cost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CostRepository extends JpaRepository<Cost, Integer> {
    
    // Find costs by vehicle ID
    List<Cost> findByVehicleIdOrderByCreatedAtDesc(Integer vehicleId);
    
    // Find costs by cost type
    List<Cost> findByCostTypeOrderByCreatedAtDesc(Cost.CostType costType);
    
    // Find costs by vehicle ID and cost type
    List<Cost> findByVehicleIdAndCostTypeOrderByCreatedAtDesc(Integer vehicleId, Cost.CostType costType);
    
    // Search costs by description
    @Query("SELECT c FROM Cost c WHERE c.description LIKE %:keyword% ORDER BY c.createdAt DESC")
    List<Cost> findByDescriptionContaining(@Param("keyword") String keyword);
    
    // Search costs by description and vehicle ID
    @Query("SELECT c FROM Cost c WHERE c.description LIKE %:keyword% AND c.vehicleId = :vehicleId ORDER BY c.createdAt DESC")
    List<Cost> findByDescriptionContainingAndVehicleId(@Param("keyword") String keyword, @Param("vehicleId") Integer vehicleId);
    
    // Search costs by description and cost type
    @Query("SELECT c FROM Cost c WHERE c.description LIKE %:keyword% AND c.costType = :costType ORDER BY c.createdAt DESC")
    List<Cost> findByDescriptionContainingAndCostType(@Param("keyword") String keyword, @Param("costType") Cost.CostType costType);
    
    // Advanced search with multiple criteria
    @Query("SELECT c FROM Cost c WHERE " +
           "(:keyword IS NULL OR c.description LIKE %:keyword%) AND " +
           "(:costType IS NULL OR c.costType = :costType) AND " +
           "(:vehicleId IS NULL OR c.vehicleId = :vehicleId) " +
           "ORDER BY c.createdAt DESC")
    List<Cost> findCostsWithFilters(@Param("keyword") String keyword, 
                                   @Param("costType") Cost.CostType costType, 
                                   @Param("vehicleId") Integer vehicleId);
    
    // Find costs by date range
    @Query("SELECT c FROM Cost c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<Cost> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    // Find costs by vehicle ID and date range
    @Query("SELECT c FROM Cost c WHERE c.vehicleId = :vehicleId AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<Cost> findByVehicleIdAndCreatedAtBetween(@Param("vehicleId") Integer vehicleId,
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    // Calculate total amount by vehicle ID
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Cost c WHERE c.vehicleId = :vehicleId")
    Double getTotalAmountByVehicleId(@Param("vehicleId") Integer vehicleId);
    
    // Calculate total amount by cost type
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Cost c WHERE c.costType = :costType")
    Double getTotalAmountByCostType(@Param("costType") Cost.CostType costType);
    
    // Calculate total amount by vehicle ID and cost type
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Cost c WHERE c.vehicleId = :vehicleId AND c.costType = :costType")
    Double getTotalAmountByVehicleIdAndCostType(@Param("vehicleId") Integer vehicleId, 
                                               @Param("costType") Cost.CostType costType);
    
    // Get all cost types with their total amounts
    @Query("SELECT c.costType, COALESCE(SUM(c.amount), 0) FROM Cost c GROUP BY c.costType")
    List<Object[]> getCostTypeSummary();
    
    // Get cost summary by vehicle
    @Query("SELECT c.vehicleId, c.costType, COALESCE(SUM(c.amount), 0) FROM Cost c GROUP BY c.vehicleId, c.costType ORDER BY c.vehicleId, c.costType")
    List<Object[]> getCostSummaryByVehicle();
    
    // Count costs by vehicle ID
    Long countByVehicleId(Integer vehicleId);
    
    // Count costs by cost type
    Long countByCostType(Cost.CostType costType);
    
    // Count costs by vehicle ID and cost type
    Long countByVehicleIdAndCostType(Integer vehicleId, Cost.CostType costType);
}