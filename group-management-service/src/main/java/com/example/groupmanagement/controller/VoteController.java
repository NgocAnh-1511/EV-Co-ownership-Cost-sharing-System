package com.example.groupmanagement.controller;

import com.example.groupmanagement.entity.GroupVote;
import com.example.groupmanagement.entity.VoteResponse;
import com.example.groupmanagement.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VoteController {

    private final VoteService voteService;

    // Vote Management APIs
    @PostMapping
    public ResponseEntity<GroupVote> createVote(@RequestBody GroupVote vote) {
        GroupVote createdVote = voteService.createVote(vote);
        return ResponseEntity.ok(createdVote);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<GroupVote>> getVotesByGroup(@PathVariable Long groupId) {
        List<GroupVote> votes = voteService.getVotesByGroup(groupId);
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupVote> getVoteById(@PathVariable Long id) {
        return voteService.getVoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupVote> updateVote(@PathVariable Long id, @RequestBody GroupVote vote) {
        return voteService.updateVote(id, vote)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVote(@PathVariable Long id) {
        voteService.deleteVote(id);
        return ResponseEntity.ok().build();
    }

    // Vote Response APIs
    @PostMapping("/{voteId}/responses")
    public ResponseEntity<VoteResponse> submitVote(@PathVariable Long voteId, @RequestBody VoteResponse response) {
        VoteResponse submittedVote = voteService.submitVote(voteId, response);
        return ResponseEntity.ok(submittedVote);
    }

    @GetMapping("/{voteId}/responses")
    public ResponseEntity<List<VoteResponse>> getVoteResponses(@PathVariable Long voteId) {
        List<VoteResponse> responses = voteService.getVoteResponses(voteId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{voteId}/results")
    public ResponseEntity<?> getVoteResults(@PathVariable Long voteId) {
        return ResponseEntity.ok(voteService.getVoteResults(voteId));
    }

    @PostMapping("/{voteId}/close")
    public ResponseEntity<GroupVote> closeVote(@PathVariable Long voteId) {
        return voteService.closeVote(voteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
