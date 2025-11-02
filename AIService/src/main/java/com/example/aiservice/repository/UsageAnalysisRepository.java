package com.example.aiservice.repository;

import com.example.aiservice.model.UsageAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsageAnalysisRepository extends JpaRepository<UsageAnalysis, Long> {
    
    List<UsageAnalysis> findByGroupId(Long groupId);
    
    List<UsageAnalysis> findByVehicleId(Long vehicleId);
    
    List<UsageAnalysis> findByUserId(Long userId);
    
    List<UsageAnalysis> findByGroupIdAndVehicleId(Long groupId, Long vehicleId);
    
    @Query("SELECT u FROM UsageAnalysis u WHERE u.groupId = :groupId " +
           "AND u.vehicleId = :vehicleId " +
           "AND u.periodStart = :periodStart " +
           "AND u.periodEnd = :periodEnd")
    List<UsageAnalysis> findByGroupIdAndVehicleIdAndPeriod(
        @Param("groupId") Long groupId,
        @Param("vehicleId") Long vehicleId,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
    
    @Query("SELECT u FROM UsageAnalysis u WHERE u.userId = :userId " +
           "AND u.vehicleId = :vehicleId " +
           "ORDER BY u.analyzedAt DESC")
    List<UsageAnalysis> findLatestByUserIdAndVehicleId(
        @Param("userId") Long userId,
        @Param("vehicleId") Long vehicleId
    );
    
    Optional<UsageAnalysis> findFirstByUserIdAndVehicleIdOrderByAnalyzedAtDesc(
        Long userId, Long vehicleId
    );
}



