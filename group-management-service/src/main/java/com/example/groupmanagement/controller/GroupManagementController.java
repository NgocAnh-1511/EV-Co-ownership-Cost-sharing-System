package com.example.groupmanagement.controller;

import com.example.groupmanagement.entity.CoOwnershipGroup;
import com.example.groupmanagement.entity.GroupMember;
import com.example.groupmanagement.service.GroupManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GroupManagementController {

    private final GroupManagementService groupManagementService;

    // Group Management APIs
    @PostMapping
    public ResponseEntity<CoOwnershipGroup> createGroup(@RequestBody CoOwnershipGroup group) {
        CoOwnershipGroup createdGroup = groupManagementService.createGroup(group);
        return ResponseEntity.ok(createdGroup);
    }

    @GetMapping
    public ResponseEntity<List<CoOwnershipGroup>> getAllGroups() {
        List<CoOwnershipGroup> groups = groupManagementService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoOwnershipGroup> getGroupById(@PathVariable Long id) {
        return groupManagementService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoOwnershipGroup> updateGroup(@PathVariable Long id, @RequestBody CoOwnershipGroup group) {
        return groupManagementService.updateGroup(id, group)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupManagementService.deleteGroup(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/{adminId}")
    public ResponseEntity<List<CoOwnershipGroup>> getGroupsByAdmin(@PathVariable String adminId) {
        List<CoOwnershipGroup> groups = groupManagementService.getGroupsByAdmin(adminId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/member/{userId}")
    public ResponseEntity<List<CoOwnershipGroup>> getGroupsByMember(@PathVariable String userId) {
        List<CoOwnershipGroup> groups = groupManagementService.getGroupsByMember(userId);
        return ResponseEntity.ok(groups);
    }

    // Member Management APIs
    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupMember> addMember(@PathVariable Long groupId, @RequestBody GroupMember member) {
        GroupMember addedMember = groupManagementService.addMember(groupId, member);
        return ResponseEntity.ok(addedMember);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMember> members = groupManagementService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @PutMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<GroupMember> updateMember(@PathVariable Long groupId, 
                                                   @PathVariable Long memberId, 
                                                   @RequestBody GroupMember member) {
        return groupManagementService.updateMember(groupId, memberId, member)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId, @PathVariable Long memberId) {
        groupManagementService.removeMember(groupId, memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupMember> getMemberByUserId(@PathVariable Long groupId, @PathVariable String userId) {
        return groupManagementService.getMemberByUserId(groupId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
