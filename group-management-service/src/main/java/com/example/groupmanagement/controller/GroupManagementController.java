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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            logger.info("=== GROUP MANAGEMENT SERVICE: Fetching all groups ===");
            List<Group> groups = groupRepository.findAll();
            logger.info("Found {} groups in database", groups.size());
            
            // Convert to DTO with member count and vote count
            List<GroupResponseDto> groupDtos = groups.stream()
                .map(group -> {
                    Integer memberCount = groupMemberRepository.countByGroup_GroupId(group.getGroupId());
                    Integer voteCount = votingRepository.countByGroup_GroupId(group.getGroupId());
                    return GroupResponseDto.fromEntity(group, memberCount, voteCount);
                })
                .collect(Collectors.toList());
            
            logger.info("Returning {} group DTOs", groupDtos.size());
            if (!groupDtos.isEmpty()) {
                logger.info("First group: ID={}, Name={}", groupDtos.get(0).getGroupId(), groupDtos.get(0).getGroupName());
            }
            
            return ResponseEntity.ok(groupDtos);
        } catch (Exception e) {
            logger.error("Error fetching groups", e);
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
    public Group createGroup(@RequestBody Map<String, Object> requestData) {
        // Extract Group data from request
        Group group = new Group();
        if (requestData.containsKey("groupName")) {
            group.setGroupName((String) requestData.get("groupName"));
        }
        if (requestData.containsKey("adminId")) {
            group.setAdminId(((Number) requestData.get("adminId")).intValue());
        }
        if (requestData.containsKey("vehicleId")) {
            Object vehicleIdObj = requestData.get("vehicleId");
            if (vehicleIdObj != null) {
                group.setVehicleId(((Number) vehicleIdObj).intValue());
            }
        }
        if (requestData.containsKey("status")) {
            String statusStr = (String) requestData.get("status");
            group.setStatus("Active".equalsIgnoreCase(statusStr) ? Group.GroupStatus.Active : Group.GroupStatus.Inactive);
        } else {
            // Default to Active if status is not provided
            group.setStatus(Group.GroupStatus.Active);
        }
        
        // Extract ownershipPercent for admin (optional)
        Double adminOwnershipPercent = null;
        if (requestData.containsKey("ownershipPercent")) {
            Object ownershipObj = requestData.get("ownershipPercent");
            if (ownershipObj != null) {
                adminOwnershipPercent = ((Number) ownershipObj).doubleValue();
            }
        }
        
        // Bước 1: Tạo Group trong database Group_Management_DB
        Group savedGroup = groupRepository.save(group);
        logger.info("✅ Created group: groupId={}, groupName={}", savedGroup.getGroupId(), savedGroup.getGroupName());

        // Bước 2: TỰ ĐỘNG thêm adminId vào GroupMember với role Admin
        try {
            GroupMember adminMember = new GroupMember();
            adminMember.setGroup(savedGroup);
            adminMember.setUserId(savedGroup.getAdminId());
            adminMember.setRole(GroupMember.MemberRole.Admin);
            // Sử dụng ownershipPercent từ request, nếu không có thì mặc định 0.0
            adminMember.setOwnershipPercent(adminOwnershipPercent != null ? adminOwnershipPercent : 0.0);
            
            GroupMember savedAdminMember = groupMemberRepository.save(adminMember);
            logger.info("✅ Auto-added admin as group member: memberId={}, userId={}, groupId={}, role=Admin, ownershipPercent={}%", 
                savedAdminMember.getMemberId(), savedAdminMember.getUserId(), savedGroup.getGroupId(), savedAdminMember.getOwnershipPercent());
        } catch (Exception e) {
            // Log lỗi nhưng vẫn trả về Group (không làm fail toàn bộ transaction)
            logger.error("❌ Failed to auto-add admin as member for groupId={}: {}", savedGroup.getGroupId(), e.getMessage());
            logger.error("Note: Group was created successfully, but admin member creation failed. Admin should be added manually.");
        }

        // Bước 3: TỰ ĐỘNG tạo Fund trong database Cost_Payment_DB
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
    public ResponseEntity<Group> updateGroup(@PathVariable Integer id, @RequestBody Map<String, Object> requestData) {
        Optional<Group> groupOpt = groupRepository.findById(id);
        if (groupOpt.isPresent()) {
            Group existingGroup = groupOpt.get();
            
            // Update fields from request
            if (requestData.containsKey("groupName")) {
                existingGroup.setGroupName((String) requestData.get("groupName"));
            }
            if (requestData.containsKey("adminId")) {
                existingGroup.setAdminId(((Number) requestData.get("adminId")).intValue());
            }
            if (requestData.containsKey("vehicleId")) {
                Object vehicleIdObj = requestData.get("vehicleId");
                if (vehicleIdObj != null) {
                    existingGroup.setVehicleId(((Number) vehicleIdObj).intValue());
                } else {
                    existingGroup.setVehicleId(null);
                }
            }
            if (requestData.containsKey("status")) {
                String statusStr = (String) requestData.get("status");
                existingGroup.setStatus("Active".equalsIgnoreCase(statusStr) ? Group.GroupStatus.Active : Group.GroupStatus.Inactive);
            }
            
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

    // Get groups by user ID (groups that user is a member of)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getGroupsByUserId(@PathVariable Integer userId) {
        try {
            logger.info("=== GROUP MANAGEMENT SERVICE: Fetching groups for userId={} ===", userId);
            
            // Get all memberships for this user
            List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
            logger.info("Found {} group memberships for userId={}", memberships.size(), userId);
            
            // Extract unique group IDs
            List<Integer> groupIds = memberships.stream()
                .map(m -> m.getGroup().getGroupId())
                .distinct()
                .collect(Collectors.toList());
            
            // Get groups for these IDs
            List<Group> groups = groupIds.stream()
                .map(groupId -> groupRepository.findById(groupId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
            
            // Convert to DTO with member count and vote count
            List<GroupResponseDto> groupDtos = groups.stream()
                .map(group -> {
                    Integer memberCount = groupMemberRepository.countByGroup_GroupId(group.getGroupId());
                    Integer voteCount = votingRepository.countByGroup_GroupId(group.getGroupId());
                    return GroupResponseDto.fromEntity(group, memberCount, voteCount);
                })
                .collect(Collectors.toList());
            
            logger.info("Returning {} groups for userId={}", groupDtos.size(), userId);
            return ResponseEntity.ok(groupDtos);
        } catch (Exception e) {
            logger.error("Error fetching groups for userId={}", userId, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to fetch groups: " + e.getMessage()));
        }
    }

    // Helper methods for permission checking
    private boolean isAdminOfGroup(Integer userId, Integer groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroup_GroupId(groupId);
        return members.stream()
            .anyMatch(m -> m.getUserId().equals(userId) && 
                          m.getRole() == GroupMember.MemberRole.Admin);
    }

    private long countAdminsInGroup(Integer groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroup_GroupId(groupId);
        return members.stream()
            .filter(m -> m.getRole() == GroupMember.MemberRole.Admin)
            .count();
    }

    private Optional<GroupMember> getMemberByUserIdAndGroupId(Integer userId, Integer groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroup_GroupId(groupId);
        return members.stream()
            .filter(m -> m.getUserId().equals(userId))
            .findFirst();
    }

    // GroupMember endpoints
    @GetMapping("/{groupId}/members")
    public List<GroupMember> getGroupMembers(@PathVariable Integer groupId) {
        return groupMemberRepository.findByGroup_GroupId(groupId);
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addGroupMember(
            @PathVariable Integer groupId, 
            @RequestBody Map<String, Object> requestData) {
        try {
            // Extract data from request
            Integer currentUserId = requestData.containsKey("currentUserId") ? 
                ((Number) requestData.get("currentUserId")).intValue() : null;
            GroupMember groupMember = new GroupMember();
            
            if (requestData.containsKey("userId")) {
                groupMember.setUserId(((Number) requestData.get("userId")).intValue());
            }
            if (requestData.containsKey("role")) {
                String roleStr = (String) requestData.get("role");
                groupMember.setRole("Admin".equalsIgnoreCase(roleStr) ? 
                    GroupMember.MemberRole.Admin : GroupMember.MemberRole.Member);
            }
            if (requestData.containsKey("ownershipPercent")) {
                groupMember.setOwnershipPercent(((Number) requestData.get("ownershipPercent")).doubleValue());
            }
            
            logger.info("🔵 [GroupManagementController] POST /api/groups/{}/members", groupId);
            logger.info("Request: currentUserId={}, userId={}, role={}, ownershipPercent={}", 
                currentUserId, groupMember.getUserId(), groupMember.getRole(), groupMember.getOwnershipPercent());
            
            // Validation: Check if currentUserId is provided
            if (currentUserId == null) {
                logger.error("❌ [GroupManagementController] currentUserId is required for authorization");
                return ResponseEntity.status(400).body(Map.of(
                    "error", "currentUserId is required",
                    "message", "Vui lòng cung cấp ID của người thực hiện thao tác"
                ));
            }
            
            // Rule 1: Kiểm tra quyền Admin
            if (!isAdminOfGroup(currentUserId, groupId)) {
                logger.warn("⚠️ [GroupManagementController] User {} is not Admin of group {}", currentUserId, groupId);
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Forbidden",
                    "message", "Chỉ Admin mới có quyền thêm thành viên vào nhóm"
                ));
            }
            
            // Validation: Check if userId is provided
            if (groupMember.getUserId() == null) {
                logger.error("❌ [GroupManagementController] userId is null");
                return ResponseEntity.status(400).body(Map.of("error", "userId is required"));
            }
            
            // Validation: Check if ownershipPercent is valid
            if (groupMember.getOwnershipPercent() == null) {
                logger.warn("⚠️ [GroupManagementController] ownershipPercent is null, setting to 0.0");
                groupMember.setOwnershipPercent(0.0);
            }
            
            // Check if group exists
            Optional<Group> group = groupRepository.findById(groupId);
            if (!group.isPresent()) {
                logger.error("❌ [GroupManagementController] Group not found: {}", groupId);
                return ResponseEntity.status(404).body(Map.of("error", "Group not found"));
            }
            
            // Rule 6: Check if user is already a member
            List<GroupMember> existingMembers = groupMemberRepository.findByGroup_GroupId(groupId);
            Optional<GroupMember> existingMemberOpt = existingMembers.stream()
                    .filter(m -> m.getUserId().equals(groupMember.getUserId()))
                    .findFirst();
            
            if (existingMemberOpt.isPresent()) {
                GroupMember existingMember = existingMemberOpt.get();
                logger.warn("⚠️ [GroupManagementController] User {} is already a member of group {} (memberId: {})", 
                    groupMember.getUserId(), groupId, existingMember.getMemberId());
                
                // If ownership percent is different, update it (still need Admin permission)
                if (groupMember.getOwnershipPercent() != null && 
                    !groupMember.getOwnershipPercent().equals(existingMember.getOwnershipPercent())) {
                    
                    // Rule 3: Validate total ownership won't exceed 100%
                    double currentTotal = existingMembers.stream()
                        .filter(m -> !m.getUserId().equals(groupMember.getUserId())) // Exclude current user
                        .mapToDouble(m -> m.getOwnershipPercent() != null ? m.getOwnershipPercent() : 0.0)
                        .sum();
                    
                    double newTotal = currentTotal + groupMember.getOwnershipPercent();
                    if (newTotal > 100.0) {
                        logger.error("❌ [GroupManagementController] Total ownership would exceed 100%: {}%", newTotal);
                        return ResponseEntity.status(400).body(Map.of(
                            "error", "Total ownership exceeds 100%",
                            "message", String.format("Tổng tỷ lệ sở hữu không được vượt quá 100%%. Hiện tại: %.2f%%", currentTotal)
                        ));
                    }
                    
                    logger.info("🔄 [GroupManagementController] Updating ownership from {} to {}", 
                        existingMember.getOwnershipPercent(), groupMember.getOwnershipPercent());
                    existingMember.setOwnershipPercent(groupMember.getOwnershipPercent());
                    GroupMember updated = groupMemberRepository.save(existingMember);
                    logger.info("✅ [GroupManagementController] Ownership updated successfully");
                    return ResponseEntity.status(200).body(updated);
                }
                
                // If same ownership, just return existing member info
                logger.info("ℹ️ [GroupManagementController] User already has same ownership, returning existing member");
                return ResponseEntity.status(200).body(existingMember);
            }
            
            // Rule 3: Validate total ownership for new member
            double currentTotal = existingMembers.stream()
                .mapToDouble(m -> m.getOwnershipPercent() != null ? m.getOwnershipPercent() : 0.0)
                .sum();
            
            double newTotal = currentTotal + (groupMember.getOwnershipPercent() != null ? groupMember.getOwnershipPercent() : 0.0);
            if (newTotal > 100.0) {
                logger.error("❌ [GroupManagementController] Total ownership would exceed 100%: {}%", newTotal);
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Total ownership exceeds 100%",
                    "message", String.format("Tổng tỷ lệ sở hữu không được vượt quá 100%%. Hiện tại: %.2f%%", currentTotal)
                ));
            }
            
            // Set group reference
            groupMember.setGroup(group.get());
            
            // Set default role if not provided
            if (groupMember.getRole() == null) {
                groupMember.setRole(GroupMember.MemberRole.Member);
                logger.info("Setting default role: Member");
            }
            
            // Save to database
            logger.info("💾 [GroupManagementController] Attempting to save member to database...");
            GroupMember saved = groupMemberRepository.save(groupMember);
            logger.info("✅ [GroupManagementController] Member added successfully: memberId={}, userId={}, groupId={}, ownershipPercent={}", 
                saved.getMemberId(), saved.getUserId(), saved.getGroup().getGroupId(), saved.getOwnershipPercent());
            
            return ResponseEntity.ok(saved);
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("❌ [GroupManagementController] Database constraint violation: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("error", "Database constraint violation", "message", e.getMessage()));
        } catch (jakarta.persistence.PersistenceException e) {
            logger.error("❌ [GroupManagementController] Persistence error: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Database error", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ [GroupManagementController] Unexpected error adding group member: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to add member", "message", e.getMessage()));
        }
    }

    @PutMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> updateGroupMember(
            @PathVariable Integer groupId,
            @PathVariable Integer memberId,
            @RequestBody Map<String, Object> requestData) {
        try {
            // Extract currentUserId from request
            Integer currentUserId = requestData.containsKey("currentUserId") ? 
                ((Number) requestData.get("currentUserId")).intValue() : null;
            
            logger.info("🔵 [GroupManagementController] PUT /api/groups/{}/members/{}", groupId, memberId);
            logger.info("Request: currentUserId={}", currentUserId);
            
            // Validation: Check if currentUserId is provided
            if (currentUserId == null) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "currentUserId is required",
                    "message", "Vui lòng cung cấp ID của người thực hiện thao tác"
                ));
            }
            
            // Rule 1: Kiểm tra quyền Admin
            if (!isAdminOfGroup(currentUserId, groupId)) {
                logger.warn("⚠️ [GroupManagementController] User {} is not Admin of group {}", currentUserId, groupId);
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Forbidden",
                    "message", "Chỉ Admin mới có quyền cập nhật thông tin thành viên"
                ));
            }
            
            Optional<GroupMember> memberOpt = groupMemberRepository.findById(memberId);
            if (!memberOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Member not found"));
            }
            
            GroupMember existingMember = memberOpt.get();
            
            // Verify member belongs to this group
            if (!existingMember.getGroup().getGroupId().equals(groupId)) {
                return ResponseEntity.status(400).body(Map.of("error", "Member does not belong to this group"));
            }
            
            // Rule 4: Không được tự thay đổi quyền của chính mình
            if (existingMember.getUserId().equals(currentUserId)) {
                String newRoleStr = requestData.containsKey("role") ? (String) requestData.get("role") : null;
                GroupMember.MemberRole currentRole = existingMember.getRole();
                GroupMember.MemberRole newRole = newRoleStr != null ? 
                    ("Admin".equalsIgnoreCase(newRoleStr) ? GroupMember.MemberRole.Admin : GroupMember.MemberRole.Member) 
                    : currentRole;
                
                if (currentRole != newRole) {
                    logger.warn("⚠️ [GroupManagementController] User {} cannot change own role", currentUserId);
                    return ResponseEntity.status(400).body(Map.of(
                        "error", "Cannot change own role",
                        "message", "Bạn không thể tự thay đổi quyền của chính mình"
                    ));
                }
            }
            
            // Rule 5: Kiểm tra khi hạ quyền Admin → Member
            String newRoleStr = requestData.containsKey("role") ? (String) requestData.get("role") : null;
            if (newRoleStr != null && existingMember.getRole() == GroupMember.MemberRole.Admin) {
                GroupMember.MemberRole newRole = "Admin".equalsIgnoreCase(newRoleStr) ? 
                    GroupMember.MemberRole.Admin : GroupMember.MemberRole.Member;
                
                if (newRole == GroupMember.MemberRole.Member) {
                    long adminCount = countAdminsInGroup(groupId);
                    if (adminCount <= 1) {
                        logger.warn("⚠️ [GroupManagementController] Cannot demote last Admin in group {}", groupId);
                        return ResponseEntity.status(400).body(Map.of(
                            "error", "Cannot demote last Admin",
                            "message", "Nhóm phải có ít nhất 1 Admin. Không thể hạ quyền Admin cuối cùng"
                        ));
                    }
                }
            }
            
            // Update fields
            if (requestData.containsKey("userId")) {
                existingMember.setUserId(((Number) requestData.get("userId")).intValue());
            }
            if (requestData.containsKey("role")) {
                String roleStr = (String) requestData.get("role");
                existingMember.setRole("Admin".equalsIgnoreCase(roleStr) ? 
                    GroupMember.MemberRole.Admin : GroupMember.MemberRole.Member);
            }
            if (requestData.containsKey("ownershipPercent")) {
                Double newOwnership = ((Number) requestData.get("ownershipPercent")).doubleValue();
                
                // Rule 3: Validate total ownership
                List<GroupMember> allMembers = groupMemberRepository.findByGroup_GroupId(groupId);
                double currentTotal = allMembers.stream()
                    .filter(m -> !m.getMemberId().equals(memberId)) // Exclude member being updated
                    .mapToDouble(m -> m.getOwnershipPercent() != null ? m.getOwnershipPercent() : 0.0)
                    .sum();
                
                double newTotal = currentTotal + newOwnership;
                if (newTotal > 100.0) {
                    return ResponseEntity.status(400).body(Map.of(
                        "error", "Total ownership exceeds 100%",
                        "message", String.format("Tổng tỷ lệ sở hữu không được vượt quá 100%%. Hiện tại: %.2f%%", currentTotal)
                    ));
                }
                
                existingMember.setOwnershipPercent(newOwnership);
            }
            
            GroupMember saved = groupMemberRepository.save(existingMember);
            logger.info("✅ [GroupManagementController] Member updated successfully: memberId={}", memberId);
            return ResponseEntity.ok(saved);
            
        } catch (Exception e) {
            logger.error("❌ [GroupManagementController] Error updating member: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update member", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> deleteGroupMember(
            @PathVariable Integer groupId, 
            @PathVariable Integer memberId,
            @RequestParam(required = false) Integer currentUserId) {
        try {
            logger.info("🔵 [GroupManagementController] DELETE /api/groups/{}/members/{}", groupId, memberId);
            logger.info("Request: currentUserId={}", currentUserId);
            
            // Validation: Check if currentUserId is provided
            if (currentUserId == null) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "currentUserId is required",
                    "message", "Vui lòng cung cấp ID của người thực hiện thao tác (thêm ?currentUserId=YOUR_ID vào URL)"
                ));
            }
            
            // Rule 1: Kiểm tra quyền Admin
            if (!isAdminOfGroup(currentUserId, groupId)) {
                logger.warn("⚠️ [GroupManagementController] User {} is not Admin of group {}", currentUserId, groupId);
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Forbidden",
                    "message", "Chỉ Admin mới có quyền xóa thành viên khỏi nhóm"
                ));
            }
            
            Optional<GroupMember> memberOpt = groupMemberRepository.findById(memberId);
            if (!memberOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Member not found"));
            }
            
            GroupMember memberToDelete = memberOpt.get();
            
            // Verify member belongs to this group
            if (!memberToDelete.getGroup().getGroupId().equals(groupId)) {
                return ResponseEntity.status(400).body(Map.of("error", "Member does not belong to this group"));
            }
            
            // Rule 4: Không được tự xóa
            if (memberToDelete.getUserId().equals(currentUserId)) {
                logger.warn("⚠️ [GroupManagementController] User {} cannot delete themselves", currentUserId);
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Cannot delete yourself",
                    "message", "Bạn không thể tự xóa chính mình khỏi nhóm"
                ));
            }
            
            // Rule 2: Không được xóa Admin cuối cùng
            if (memberToDelete.getRole() == GroupMember.MemberRole.Admin) {
                long adminCount = countAdminsInGroup(groupId);
                if (adminCount <= 1) {
                    logger.warn("⚠️ [GroupManagementController] Cannot delete last Admin in group {}", groupId);
                    return ResponseEntity.status(400).body(Map.of(
                        "error", "Cannot delete last Admin",
                        "message", "Không thể xóa Admin cuối cùng trong nhóm"
                    ));
                }
            }
            
            groupMemberRepository.deleteById(memberId);
            logger.info("✅ [GroupManagementController] Member deleted successfully: memberId={}", memberId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("❌ [GroupManagementController] Error deleting member: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete member", "message", e.getMessage()));
        }
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
    public ResponseEntity<?> submitVote(@PathVariable Integer voteId, @RequestBody Map<String, Object> voteData) {
        try {
            Optional<Voting> votingOpt = votingRepository.findById(voteId);
            if (!votingOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Voting not found"));
            }
            
            Voting voting = votingOpt.get();
            if (voting.getGroup() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Voting is not associated with a group"));
            }
            Integer groupId = voting.getGroup().getGroupId();
            
            // Get memberId from request
            Integer memberId = null;
            if (voteData.containsKey("memberId")) {
                memberId = Integer.valueOf(voteData.get("memberId").toString());
            } else if (voteData.containsKey("userId")) {
                // If userId is provided, find the memberId
                Integer userId = Integer.valueOf(voteData.get("userId").toString());
                Optional<GroupMember> memberOpt = getMemberByUserIdAndGroupId(userId, groupId);
                if (!memberOpt.isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "User is not a member of this group"));
                }
                memberId = memberOpt.get().getMemberId();
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "memberId or userId is required"));
            }
            
            // Get choice from request
            if (!voteData.containsKey("choice") || voteData.get("choice") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "choice is required"));
            }
            String choiceStr = voteData.get("choice").toString();
            VotingResult.VoteChoice choice;
            try {
                choice = VotingResult.VoteChoice.valueOf(choiceStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid choice value. Must be A (Agree) or D (Disagree)"));
            }
            
            // Check if user already voted - use final copy for lambda
            final Integer finalMemberId = memberId;
            List<VotingResult> existingVotes = votingResultRepository.findByVoting_VoteId(voteId);
            boolean alreadyVoted = existingVotes.stream()
                .anyMatch(vr -> vr.getGroupMember().getMemberId().equals(finalMemberId));
            
            if (alreadyVoted) {
                return ResponseEntity.badRequest().body(Map.of("error", "You have already voted on this decision"));
            }
            
            // Get GroupMember
            Optional<GroupMember> memberOpt = groupMemberRepository.findById(memberId);
            if (!memberOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Member not found"));
            }
            
            // Create and save voting result
            VotingResult votingResult = new VotingResult();
            votingResult.setVoting(voting);
            votingResult.setGroupMember(memberOpt.get());
            votingResult.setChoice(choice);
            votingResult = votingResultRepository.save(votingResult);
            
            // Update total votes count
            voting.setTotalVotes(voting.getTotalVotes() + 1);
            
            // Check if decision should be accepted
            List<VotingResult> allVotes = votingResultRepository.findByVoting_VoteId(voteId);
            long agreeVotes = allVotes.stream()
                .filter(vr -> vr.getChoice() == VotingResult.VoteChoice.A)
                .count();
            
            double agreePercentage = allVotes.size() > 0 
                ? (double) agreeVotes / allVotes.size() * 100 
                : 0;
            
            // Check if admin agreed
            Group group = voting.getGroup();
            Integer adminId = group.getAdminId();
            
            // Find admin member - prioritize by adminId, then by Admin role
            List<GroupMember> groupMembers = groupMemberRepository.findByGroup_GroupId(groupId);
            Optional<GroupMember> adminMemberOpt = groupMembers.stream()
                .filter(m -> m.getUserId().equals(adminId))
                .findFirst()
                .or(() -> groupMembers.stream()
                    .filter(m -> m.getRole() == GroupMember.MemberRole.Admin)
                    .findFirst());
            
            boolean adminAgreed = false;
            if (adminMemberOpt.isPresent()) {
                Integer adminMemberId = adminMemberOpt.get().getMemberId();
                adminAgreed = allVotes.stream()
                    .anyMatch(vr -> vr.getGroupMember().getMemberId().equals(adminMemberId) 
                        && vr.getChoice() == VotingResult.VoteChoice.A);
            }
            
            // If >50% agree AND admin agreed, set final result
            if (agreePercentage > 50 && adminAgreed && voting.getFinalResult() == null) {
                voting.setFinalResult("Đã chấp nhận");
                votingRepository.save(voting);
            } else if (agreePercentage <= 50 && voting.getFinalResult() == null) {
                // Check if all members have voted
                List<GroupMember> allMembers = groupMemberRepository.findByGroup_GroupId(groupId);
                if (allVotes.size() >= allMembers.size()) {
                    // All members voted but condition not met
                    voting.setFinalResult("Đã từ chối");
                    votingRepository.save(voting);
                }
            }
            
            // Build response map - handle null finalResult
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("votingResult", votingResult);
            response.put("agreePercentage", agreePercentage);
            response.put("adminAgreed", adminAgreed);
            response.put("finalResult", voting.getFinalResult() != null ? voting.getFinalResult() : "");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error submitting vote", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";
            return ResponseEntity.status(500).body(Map.of("error", errorMessage));
        }
    }
}