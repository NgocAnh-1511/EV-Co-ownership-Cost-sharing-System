package com.example.groupmanagement.repository;

import com.example.groupmanagement.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    List<GroupMember> findByGroupId(Long groupId);
    
    List<GroupMember> findByUserId(String userId);
    
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, String userId);
    
    List<GroupMember> findByGroupIdAndRole(Long groupId, GroupMember.MemberRole role);
    
    List<GroupMember> findByGroupIdAndStatus(Long groupId, GroupMember.MemberStatus status);
    
    @Query("SELECT SUM(m.ownershipPercentage) FROM GroupMember m WHERE m.group.id = :groupId AND m.status = 'ACTIVE'")
    Double getTotalOwnershipPercentage(@Param("groupId") Long groupId);
    
    @Query("SELECT COUNT(m) FROM GroupMember m WHERE m.group.id = :groupId AND m.status = 'ACTIVE'")
    Long countActiveMembers(@Param("groupId") Long groupId);
}
