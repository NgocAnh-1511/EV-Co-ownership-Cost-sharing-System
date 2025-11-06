package com.example.aiservice.repository;

import com.example.aiservice.model.AIRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {
    
    List<AIRecommendation> findByGroupId(Long groupId);
    
    List<AIRecommendation> findByVehicleId(Long vehicleId);
    
    List<AIRecommendation> findByTargetUserId(Long targetUserId);
    
    List<AIRecommendation> findByGroupIdAndStatus(Long groupId, AIRecommendation.Status status);
    
    @Query("SELECT r FROM AIRecommendation r WHERE r.groupId = :groupId " +
           "AND r.status = :status ORDER BY r.severity DESC, r.createdAt DESC")
    List<AIRecommendation> findByGroupIdAndStatusOrderBySeverity(
        @Param("groupId") Long groupId,
        @Param("status") AIRecommendation.Status status
    );
    
    @Query("SELECT r FROM AIRecommendation r WHERE r.targetUserId = :userId " +
           "AND r.status = :status ORDER BY r.severity DESC, r.createdAt DESC")
    List<AIRecommendation> findByTargetUserIdAndStatusOrderBySeverity(
        @Param("userId") Long userId,
        @Param("status") AIRecommendation.Status status
    );
    
    @Query("SELECT r FROM AIRecommendation r WHERE r.groupId = :groupId " +
           "AND r.vehicleId = :vehicleId " +
           "AND r.periodStart = :periodStart " +
           "AND r.periodEnd = :periodEnd")
    List<AIRecommendation> findByGroupIdAndVehicleIdAndPeriod(
        @Param("groupId") Long groupId,
        @Param("vehicleId") Long vehicleId,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
    
    // New methods for UI
    List<AIRecommendation> findByType(AIRecommendation.RecommendationType type);
    
    List<AIRecommendation> findBySeverity(AIRecommendation.Severity severity);
    
    List<AIRecommendation> findByTargetUserIdAndStatus(Long targetUserId, AIRecommendation.Status status);
    
    @Query("SELECT r FROM AIRecommendation r ORDER BY r.createdAt DESC")
    List<AIRecommendation> findAll();
}

