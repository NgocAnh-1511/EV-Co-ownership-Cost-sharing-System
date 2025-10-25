package com.example.groupmanagement.repository;

import com.example.groupmanagement.entity.VoteResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteResponseRepository extends JpaRepository<VoteResponse, Long> {
    
    List<VoteResponse> findByVoteId(Long voteId);
    
    List<VoteResponse> findByUserId(String userId);
    
    Optional<VoteResponse> findByVoteIdAndUserId(Long voteId, String userId);
}
