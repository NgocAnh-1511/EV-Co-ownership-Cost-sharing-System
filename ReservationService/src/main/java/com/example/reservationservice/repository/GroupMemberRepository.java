package com.example.reservationservice.repository;

import com.example.reservationservice.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroup_GroupId(Long groupId);
    List<GroupMember> findByUser_UserId(Long userId);
    
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.userId = :userId")
    List<GroupMember> findGroupsByUserId(@Param("userId") Long userId);
}
