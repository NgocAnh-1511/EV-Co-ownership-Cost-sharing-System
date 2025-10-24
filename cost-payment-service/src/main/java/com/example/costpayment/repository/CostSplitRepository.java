package com.example.costpayment.repository;

import com.example.costpayment.entity.CostSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostSplitRepository extends JpaRepository<CostSplit, Long> {
    
    List<CostSplit> findByCostItemId(Long costItemId);
    
    List<CostSplit> findByUserId(String userId);
    
    List<CostSplit> findByUserIdAndStatus(String userId, CostSplit.SplitStatus status);
    
    @Query("SELECT cs FROM CostSplit cs WHERE cs.costItem.groupId = :groupId")
    List<CostSplit> findByGroupId(@Param("groupId") String groupId);
    
    @Query("SELECT SUM(cs.splitAmount) FROM CostSplit cs WHERE cs.userId = :userId AND cs.status = 'PAID'")
    Double getTotalPaidByUser(@Param("userId") String userId);
    
    @Query("SELECT SUM(cs.splitAmount - cs.paidAmount) FROM CostSplit cs WHERE cs.userId = :userId AND cs.status = 'PENDING'")
    Double getTotalOutstandingByUser(@Param("userId") String userId);
}
