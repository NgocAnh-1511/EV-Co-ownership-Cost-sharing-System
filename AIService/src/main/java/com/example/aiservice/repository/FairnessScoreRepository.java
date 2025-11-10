package com.example.aiservice.repository;

import com.example.aiservice.model.FairnessScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FairnessScoreRepository extends JpaRepository<FairnessScore, Long> {
    
    List<FairnessScore> findByGroupId(Long groupId);
    
    List<FairnessScore> findByVehicleId(Long vehicleId);
    
    List<FairnessScore> findByUserId(Long userId);
    
    List<FairnessScore> findByGroupIdAndVehicleId(Long groupId, Long vehicleId);
    
    @Query("SELECT f FROM FairnessScore f WHERE f.groupId = :groupId " +
           "AND f.vehicleId = :vehicleId " +
           "AND f.periodStart = :periodStart " +
           "AND f.periodEnd = :periodEnd")
    List<FairnessScore> findByGroupIdAndVehicleIdAndPeriod(
        @Param("groupId") Long groupId,
        @Param("vehicleId") Long vehicleId,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
    
    Optional<FairnessScore> findFirstByUserIdAndVehicleIdOrderByCalculatedAtDesc(
        Long userId, Long vehicleId
    );
    
    @Query("SELECT AVG(f.fairnessScore) FROM FairnessScore f " +
           "WHERE f.groupId = :groupId AND f.vehicleId = :vehicleId " +
           "AND f.periodStart = :periodStart AND f.periodEnd = :periodEnd")
    Double calculateGroupAverageFairness(
        @Param("groupId") Long groupId,
        @Param("vehicleId") Long vehicleId,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
}
















