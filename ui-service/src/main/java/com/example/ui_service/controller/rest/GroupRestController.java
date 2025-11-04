package com.example.ui_service.controller.rest;

import com.example.ui_service.client.GroupManagementClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller để proxy các request Groups từ frontend sang backend
 */
@RestController
@RequestMapping("/api/groups")
public class GroupRestController {

    @Autowired
    private GroupManagementClient groupManagementClient;

    /**
     * Lấy tất cả groups
     * GET /api/groups
     */
    @GetMapping
    public ResponseEntity<?> getAllGroups() {
        try {
            System.out.println("=== UI SERVICE: Fetching all groups ===");
            List<Map<String, Object>> groups = groupManagementClient.getAllGroupsAsMap();
            System.out.println("Groups retrieved: " + (groups != null ? groups.size() : 0));
            if (groups != null && !groups.isEmpty()) {
                System.out.println("First group: " + groups.get(0));
            }
            return ResponseEntity.ok(groups != null ? groups : java.util.Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error fetching groups: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to fetch groups: " + e.getMessage()));
        }
    }

    /**
     * Lấy groups theo userId (các nhóm mà user đã tham gia)
     * GET /api/groups/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getGroupsByUserId(@PathVariable Integer userId) {
        try {
            System.out.println("=== UI SERVICE: Fetching groups for userId=" + userId + " ===");
            List<Map<String, Object>> groups = groupManagementClient.getGroupsByUserIdAsMap(userId);
            System.out.println("Groups retrieved for userId " + userId + ": " + (groups != null ? groups.size() : 0));
            return ResponseEntity.ok(groups != null ? groups : java.util.Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error fetching groups for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to fetch groups: " + e.getMessage()));
        }
    }

    /**
     * Lấy group theo ID
     * GET /api/groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getGroupById(@PathVariable Integer id) {
        try {
            Map<String, Object> group = groupManagementClient.getGroupByIdAsMap(id);
            if (group != null) {
                return ResponseEntity.ok(group);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error fetching group: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tạo group mới
     * POST /api/groups
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody Map<String, Object> groupData) {
        try {
            Map<String, Object> createdGroup = groupManagementClient.createGroupAsMap(groupData);
            if (createdGroup != null) {
                return ResponseEntity.ok(createdGroup);
            } else {
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception e) {
            System.err.println("Error creating group: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cập nhật group
     * PUT /api/groups/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateGroup(@PathVariable Integer id, @RequestBody Map<String, Object> groupData) {
        try {
            Map<String, Object> updatedGroup = groupManagementClient.updateGroupAsMap(id, groupData);
            if (updatedGroup != null) {
                return ResponseEntity.ok(updatedGroup);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error updating group: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xóa group
     * DELETE /api/groups/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Integer id) {
        try {
            groupManagementClient.deleteGroup(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting group: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy members của group
     * GET /api/groups/{groupId}/members
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<Map<String, Object>>> getGroupMembers(@PathVariable Integer groupId) {
        try {
            List<Map<String, Object>> members = groupManagementClient.getGroupMembersAsMap(groupId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            System.err.println("Error fetching group members: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Thêm member vào group
     * POST /api/groups/{groupId}/members
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<Map<String, Object>> addGroupMember(@PathVariable Integer groupId, @RequestBody Map<String, Object> memberData) {
        try {
            Map<String, Object> createdMember = groupManagementClient.addGroupMemberAsMap(groupId, memberData);
            if (createdMember != null) {
                return ResponseEntity.ok(createdMember);
            } else {
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception e) {
            System.err.println("Error adding group member: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cập nhật member
     * PUT /api/groups/{groupId}/members/{memberId}
     */
    @PutMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Map<String, Object>> updateGroupMember(
            @PathVariable Integer groupId, 
            @PathVariable Integer memberId, 
            @RequestBody Map<String, Object> memberData) {
        try {
            Map<String, Object> updatedMember = groupManagementClient.updateGroupMemberAsMap(groupId, memberId, memberData);
            if (updatedMember != null) {
                return ResponseEntity.ok(updatedMember);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error updating group member: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xóa member
     * DELETE /api/groups/{groupId}/members/{memberId}
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> deleteGroupMember(@PathVariable Integer groupId, @PathVariable Integer memberId) {
        try {
            groupManagementClient.deleteGroupMember(groupId, memberId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting group member: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy votes của group
     * GET /api/groups/{groupId}/votes
     */
    @GetMapping("/{groupId}/votes")
    public ResponseEntity<List<Map<String, Object>>> getGroupVotes(@PathVariable Integer groupId) {
        try {
            List<Map<String, Object>> votes = groupManagementClient.getGroupVotesAsMap(groupId);
            return ResponseEntity.ok(votes);
        } catch (Exception e) {
            System.err.println("Error fetching group votes: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

