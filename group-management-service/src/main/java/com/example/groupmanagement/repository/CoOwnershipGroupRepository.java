package com.example.groupmanagement.repository;

import com.example.groupmanagement.entity.CoOwnershipGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoOwnershipGroupRepository extends JpaRepository<CoOwnershipGroup, Long> {
    
    Optional<CoOwnershipGroup> findByGroupName(String groupName);
    
    List<CoOwnershipGroup> findByGroupAdminId(String adminId);
    
    List<CoOwnershipGroup> findByVehicleId(String vehicleId);
    
    List<CoOwnershipGroup> findByStatus(CoOwnershipGroup.GroupStatus status);
    
    @Query("SELECT g FROM CoOwnershipGroup g JOIN g.members m WHERE m.userId = :userId")
    List<CoOwnershipGroup> findByMemberUserId(@Param("userId") String userId);
    
    @Query("SELECT g FROM CoOwnershipGroup g WHERE g.groupName LIKE %:keyword% OR g.description LIKE %:keyword%")
    List<CoOwnershipGroup> searchByKeyword(@Param("keyword") String keyword);
}
