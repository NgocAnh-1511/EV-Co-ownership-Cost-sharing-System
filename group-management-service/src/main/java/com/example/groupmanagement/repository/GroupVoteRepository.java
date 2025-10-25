package com.example.groupmanagement.repository;

import com.example.groupmanagement.entity.GroupVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupVoteRepository extends JpaRepository<GroupVote, Long> {
    
    List<GroupVote> findByGroupId(Long groupId);
    
    List<GroupVote> findByStatus(GroupVote.VoteStatus status);
}
