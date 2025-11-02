package com.example.groupmanagement.controller;

import com.example.groupmanagement.dto.GroupResponseDto;
import com.example.groupmanagement.entity.Group;
import com.example.groupmanagement.entity.GroupMember;
import com.example.groupmanagement.entity.Voting;
import com.example.groupmanagement.entity.VotingResult;
import com.example.groupmanagement.repository.GroupRepository;
import com.example.groupmanagement.repository.GroupMemberRepository;
import com.example.groupmanagement.repository.VotingRepository;
import com.example.groupmanagement.repository.VotingResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupManagementController {

    private static final Logger logger = LoggerFactory.getLogger(GroupManagementController.class);

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private VotingResultRepository votingResultRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${cost-payment.service.url:http://localhost:8081}")
    private String costPaymentServiceUrl;

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
            
            // Convert to DTO with member count and vote count
            List<GroupResponseDto> groupDtos = groups.stream()
                .map(group -> {
                    Integer memberCount = groupMemberRepository.countByGroup_GroupId(group.getGroupId());
                    Integer voteCount = votingRepository.countByGroup_GroupId(group.getGroupId());
                    return GroupResponseDto.fromEntity(group, memberCount, voteCount);
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(groupDtos);
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
        // Bước 1: Tạo Group trong database Group_Management_DB
        Group savedGroup = groupRepository.save(group);
        logger.info("✅ Created group: groupId={}, groupName={}", savedGroup.getGroupId(), savedGroup.getGroupName());

        // Bước 2: TỰ ĐỘNG tạo Fund trong database Cost_Payment_DB
        try {
            String fundCreateUrl = costPaymentServiceUrl + "/api/funds/group/" + savedGroup.getGroupId();
            logger.info("🔄 Auto-creating fund for groupId={} at URL: {}", savedGroup.getGroupId(), fundCreateUrl);
            
            ResponseEntity<String> response = restTemplate.postForEntity(fundCreateUrl, null, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ Fund auto-created successfully for groupId={}", savedGroup.getGroupId());
            } else {
                logger.warn("⚠️ Fund creation returned status: {} for groupId={}", response.getStatusCode(), savedGroup.getGroupId());
            }
        } catch (Exception e) {
            // Log lỗi nhưng vẫn trả về Group (không làm fail toàn bộ transaction)
            logger.error("❌ Failed to auto-create fund for groupId={}: {}", savedGroup.getGroupId(), e.getMessage());
            logger.error("Note: Group was created successfully, but fund creation failed. Admin should create fund manually.");
        }

        return savedGroup;
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

    @PutMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<GroupMember> updateGroupMember(
            @PathVariable Integer groupId,
            @PathVariable Integer memberId,
            @RequestBody GroupMember memberDetails) {
        Optional<GroupMember> member = groupMemberRepository.findById(memberId);
        if (member.isPresent()) {
            GroupMember existingMember = member.get();
            existingMember.setUserId(memberDetails.getUserId());
            existingMember.setRole(memberDetails.getRole());
            existingMember.setOwnershipPercent(memberDetails.getOwnershipPercent());
            return ResponseEntity.ok(groupMemberRepository.save(existingMember));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> deleteGroupMember(@PathVariable Integer groupId, @PathVariable Integer memberId) {
        if (groupMemberRepository.existsById(memberId)) {
            groupMemberRepository.deleteById(memberId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
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