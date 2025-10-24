package com.example.groupmanagement.service;

import com.example.groupmanagement.entity.GroupVote;
import com.example.groupmanagement.entity.VoteResponse;
import com.example.groupmanagement.repository.GroupVoteRepository;
import com.example.groupmanagement.repository.VoteResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteService {

    private final GroupVoteRepository voteRepository;
    private final VoteResponseRepository responseRepository;

    public GroupVote createVote(GroupVote vote) {
        vote.setStatus(GroupVote.VoteStatus.ACTIVE);
        return voteRepository.save(vote);
    }

    public List<GroupVote> getVotesByGroup(Long groupId) {
        return voteRepository.findByGroupId(groupId);
    }

    public Optional<GroupVote> getVoteById(Long id) {
        return voteRepository.findById(id);
    }

    public Optional<GroupVote> updateVote(Long id, GroupVote vote) {
        return voteRepository.findById(id)
                .map(existingVote -> {
                    existingVote.setTitle(vote.getTitle());
                    existingVote.setDescription(vote.getDescription());
                    existingVote.setEndDate(vote.getEndDate());
                    return voteRepository.save(existingVote);
                });
    }

    public void deleteVote(Long id) {
        voteRepository.deleteById(id);
    }

    public VoteResponse submitVote(Long voteId, VoteResponse response) {
        GroupVote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found"));

        // Check if vote is still active
        if (vote.getStatus() != GroupVote.VoteStatus.ACTIVE) {
            throw new IllegalArgumentException("Vote is no longer active");
        }

        // Check if vote has expired
        if (LocalDateTime.now().isAfter(vote.getEndDate())) {
            throw new IllegalArgumentException("Vote has expired");
        }

        // Check if user has already voted
        if (responseRepository.findByVoteIdAndUserId(voteId, response.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User has already voted");
        }

        response.setVote(vote);
        return responseRepository.save(response);
    }

    public List<VoteResponse> getVoteResponses(Long voteId) {
        return responseRepository.findByVoteId(voteId);
    }

    public Map<String, Object> getVoteResults(Long voteId) {
        GroupVote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found"));

        List<VoteResponse> responses = responseRepository.findByVoteId(voteId);
        
        Map<String, Object> results = new HashMap<>();
        results.put("voteId", voteId);
        results.put("title", vote.getTitle());
        results.put("totalResponses", responses.size());
        results.put("status", vote.getStatus());
        
        // Calculate results by option
        Map<String, Integer> optionResults = new HashMap<>();
        for (VoteResponse response : responses) {
            String optionText = response.getSelectedOption().getOptionText();
            optionResults.put(optionText, optionResults.getOrDefault(optionText, 0) + 1);
        }
        
        results.put("results", optionResults);
        
        return results;
    }

    public Optional<GroupVote> closeVote(Long voteId) {
        return voteRepository.findById(voteId)
                .map(vote -> {
                    vote.setStatus(GroupVote.VoteStatus.EXPIRED);
                    return voteRepository.save(vote);
                });
    }
}
