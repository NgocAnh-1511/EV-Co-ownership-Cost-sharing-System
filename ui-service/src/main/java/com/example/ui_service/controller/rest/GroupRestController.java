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
            System.out.println("🔵 [GroupRestController] POST /api/groups/" + groupId + "/members");
            System.out.println("Request data: " + memberData);
            
            Map<String, Object> createdMember = groupManagementClient.addGroupMemberAsMap(groupId, memberData);
            if (createdMember != null) {
                System.out.println("✅ [GroupRestController] Member added successfully: " + createdMember);
                return ResponseEntity.ok(createdMember);
            } else {
                System.err.println("❌ [GroupRestController] addGroupMemberAsMap returned null");
                return ResponseEntity.status(500).body(Map.of("error", "Failed to add member", "message", "Group member service returned null"));
            }
        } catch (RuntimeException e) {
            System.err.println("❌ [GroupRestController] Error adding group member: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to add member", "message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [GroupRestController] Unexpected error adding group member: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to add member", "message", e.getMessage()));
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

    /**
     * Submit vote cho một quyết định
     * POST /api/groups/votes/{voteId}/results
     */
    @PostMapping("/votes/{voteId}/results")
    public ResponseEntity<?> submitVote(@PathVariable Integer voteId, @RequestBody Map<String, Object> voteData) {
        try {
            Map<String, Object> result = groupManagementClient.submitVoteAsMap(voteId, voteData);
            if (result != null && result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error submitting vote: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to submit vote: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin membership của user trong nhóm
     * GET /api/groups/{groupId}/members/me/{userId}
     */
    @GetMapping("/{groupId}/members/me/{userId}")
    public ResponseEntity<?> getMyMembershipInfo(@PathVariable Integer groupId, @PathVariable Integer userId) {
        try {
            Map<String, Object> result = groupManagementClient.getMyMembershipInfo(groupId, userId);
            if (result != null && result.containsKey("error")) {
                return ResponseEntity.status(404).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error getting membership info: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get membership info", "message", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách thành viên trong nhóm (cho user xem)
     * GET /api/groups/{groupId}/members/view
     */
    @GetMapping("/{groupId}/members/view")
    public ResponseEntity<?> viewGroupMembers(@PathVariable Integer groupId) {
        try {
            Map<String, Object> result = groupManagementClient.viewGroupMembers(groupId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error viewing group members: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to view members", "message", e.getMessage()));
        }
    }

    /**
     * User tạo yêu cầu rời nhóm
     * POST /api/groups/{groupId}/leave-request
     */
    @PostMapping("/{groupId}/leave-request")
    public ResponseEntity<?> createLeaveRequest(@PathVariable Integer groupId, @RequestBody Map<String, Object> requestData) {
        try {
            Map<String, Object> result = groupManagementClient.createLeaveRequest(groupId, requestData);
            if (result != null && result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error creating leave request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create leave request", "message", e.getMessage()));
        }
    }

    /**
     * Admin xem các yêu cầu rời nhóm
     * GET /api/groups/{groupId}/leave-requests
     */
    @GetMapping("/{groupId}/leave-requests")
    public ResponseEntity<?> getLeaveRequests(
            @PathVariable Integer groupId,
            @RequestParam(required = false) Integer currentUserId) {
        try {
            Map<String, Object> result = groupManagementClient.getLeaveRequests(groupId, currentUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error getting leave requests: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get leave requests", "message", e.getMessage()));
        }
    }

    /**
     * Admin phê duyệt yêu cầu rời nhóm
     * POST /api/groups/{groupId}/leave-requests/{requestId}/approve
     */
    @PostMapping("/{groupId}/leave-requests/{requestId}/approve")
    public ResponseEntity<?> approveLeaveRequest(
            @PathVariable Integer groupId,
            @PathVariable Integer requestId,
            @RequestBody(required = false) Map<String, Object> requestData) {
        try {
            Map<String, Object> result = groupManagementClient.approveLeaveRequest(groupId, requestId, requestData);
            if (result != null && result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error approving leave request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to approve leave request", "message", e.getMessage()));
        }
    }

    /**
     * Admin từ chối yêu cầu rời nhóm
     * POST /api/groups/{groupId}/leave-requests/{requestId}/reject
     */
    @PostMapping("/{groupId}/leave-requests/{requestId}/reject")
    public ResponseEntity<?> rejectLeaveRequest(
            @PathVariable Integer groupId,
            @PathVariable Integer requestId,
            @RequestBody(required = false) Map<String, Object> requestData) {
        try {
            Map<String, Object> result = groupManagementClient.rejectLeaveRequest(groupId, requestId, requestData);
            if (result != null && result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error rejecting leave request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to reject leave request", "message", e.getMessage()));
        }
    }

    /**
     * User xem trạng thái yêu cầu rời nhóm của mình
     * GET /api/groups/{groupId}/leave-requests/me/{userId}
     */
    @GetMapping("/{groupId}/leave-requests/me/{userId}")
    public ResponseEntity<?> getMyLeaveRequestStatus(@PathVariable Integer groupId, @PathVariable Integer userId) {
        try {
            Map<String, Object> result = groupManagementClient.getMyLeaveRequestStatus(groupId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error getting leave request status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get leave request status", "message", e.getMessage()));
        }
    }
}

