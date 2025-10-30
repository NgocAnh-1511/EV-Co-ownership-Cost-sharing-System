package com.example.groupmanagement.controller;

import com.example.groupmanagement.entity.Group;
import com.example.groupmanagement.entity.GroupMember;
import com.example.groupmanagement.entity.Voting;
import com.example.groupmanagement.entity.VotingResult;
import com.example.groupmanagement.repository.GroupRepository;
import com.example.groupmanagement.repository.GroupMemberRepository;
import com.example.groupmanagement.repository.VotingRepository;
import com.example.groupmanagement.repository.VotingResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupManagementController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private VotingResultRepository votingResultRepository;

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            long count = groupRepository.count();
            return ResponseEntity.ok("Database connected. Groups count: " + count);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Database error: " + e.getMessage());
        }
    }

    // Group endpoints
    @GetMapping
    public ResponseEntity<?> getAllGroups() {
        try {
            List<Group> groups = groupRepository.findAll();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage() + " - Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Integer id) {
        Optional<Group> group = groupRepository.findById(id);
        return group.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Group createGroup(@RequestBody Group group) {
        return groupRepository.save(group);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable Integer id, @RequestBody Group groupDetails) {
        Optional<Group> group = groupRepository.findById(id);
        if (group.isPresent()) {
            Group existingGroup = group.get();
            existingGroup.setGroupName(groupDetails.getGroupName());
            existingGroup.setAdminId(groupDetails.getAdminId());
            existingGroup.setVehicleId(groupDetails.getVehicleId());
            existingGroup.setStatus(groupDetails.getStatus());
            return ResponseEntity.ok(groupRepository.save(existingGroup));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Integer id) {
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GroupMember endpoints
    @GetMapping("/{groupId}/members")
    public List<GroupMember> getGroupMembers(@PathVariable Integer groupId) {
        return groupMemberRepository.findByGroup_GroupId(groupId);
    }

    @PostMapping("/{groupId}/members")
    public GroupMember addGroupMember(@PathVariable Integer groupId, @RequestBody GroupMember groupMember) {
        Optional<Group> group = groupRepository.findById(groupId);
        if (group.isPresent()) {
            groupMember.setGroup(group.get());
            return groupMemberRepository.save(groupMember);
        }
        return null;
    }

    // Voting endpoints
    @GetMapping("/{groupId}/votes")
    public List<Voting> getGroupVotes(@PathVariable Integer groupId) {
        return votingRepository.findByGroup_GroupId(groupId);
    }

    @PostMapping("/{groupId}/votes")
    public Voting createVote(@PathVariable Integer groupId, @RequestBody Voting voting) {
        Optional<Group> group = groupRepository.findById(groupId);
        if (group.isPresent()) {
            voting.setGroup(group.get());
            return votingRepository.save(voting);
        }
        return null;
    }

    // VotingResult endpoints
    @PostMapping("/votes/{voteId}/results")
    public VotingResult submitVote(@PathVariable Integer voteId, @RequestBody VotingResult votingResult) {
        Optional<Voting> voting = votingRepository.findById(voteId);
        if (voting.isPresent()) {
            votingResult.setVoting(voting.get());
            return votingResultRepository.save(votingResult);
        }
        return null;
    }
}