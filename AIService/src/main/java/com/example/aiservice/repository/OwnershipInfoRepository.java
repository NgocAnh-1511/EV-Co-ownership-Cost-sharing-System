package com.example.aiservice.repository;

import com.example.aiservice.model.OwnershipInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnershipInfoRepository extends JpaRepository<OwnershipInfo, Long> {
    
    List<OwnershipInfo> findByGroupId(Long groupId);
    
    List<OwnershipInfo> findByVehicleId(Long vehicleId);
    
    Optional<OwnershipInfo> findByUserIdAndVehicleId(Long userId, Long vehicleId);
    
    List<OwnershipInfo> findByUserId(Long userId);
    
    List<OwnershipInfo> findByGroupIdAndVehicleId(Long groupId, Long vehicleId);
    
    boolean existsByUserIdAndVehicleId(Long userId, Long vehicleId);
}



