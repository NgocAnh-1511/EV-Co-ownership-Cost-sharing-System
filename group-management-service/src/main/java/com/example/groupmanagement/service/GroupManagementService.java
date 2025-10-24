package com.example.groupmanagement.service;

import com.example.groupmanagement.entity.CoOwnershipGroup;
import com.example.groupmanagement.entity.GroupMember;
import com.example.groupmanagement.repository.CoOwnershipGroupRepository;
import com.example.groupmanagement.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupManagementService {

    private final CoOwnershipGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;

    public CoOwnershipGroup createGroup(CoOwnershipGroup group) {
        // Validate total ownership percentage
        if (group.getTotalOwnershipPercentage() != 100.0) {
            throw new IllegalArgumentException("Total ownership percentage must be 100%");
        }
        return groupRepository.save(group);
    }

    public List<CoOwnershipGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    public Optional<CoOwnershipGroup> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    public Optional<CoOwnershipGroup> updateGroup(Long id, CoOwnershipGroup group) {
        return groupRepository.findById(id)
                .map(existingGroup -> {
                    existingGroup.setGroupName(group.getGroupName());
                    existingGroup.setDescription(group.getDescription());
                    existingGroup.setStatus(group.getStatus());
                    return groupRepository.save(existingGroup);
                });
    }

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    public List<CoOwnershipGroup> getGroupsByAdmin(String adminId) {
        return groupRepository.findByGroupAdminId(adminId);
    }

    public List<CoOwnershipGroup> getGroupsByMember(String userId) {
        return groupRepository.findByMemberUserId(userId);
    }

    public GroupMember addMember(Long groupId, GroupMember member) {
        CoOwnershipGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Check if user is already a member
        if (memberRepository.findByGroupIdAndUserId(groupId, member.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        // Validate ownership percentage
        Double currentTotal = memberRepository.getTotalOwnershipPercentage(groupId);
        if (currentTotal + member.getOwnershipPercentage() > 100.0) {
            throw new IllegalArgumentException("Total ownership percentage cannot exceed 100%");
        }

        member.setGroup(group);
        return memberRepository.save(member);
    }

    public List<GroupMember> getGroupMembers(Long groupId) {
        return memberRepository.findByGroupId(groupId);
    }

    public Optional<GroupMember> updateMember(Long groupId, Long memberId, GroupMember member) {
        return memberRepository.findById(memberId)
                .filter(m -> m.getGroup().getId().equals(groupId))
                .map(existingMember -> {
                    existingMember.setOwnershipPercentage(member.getOwnershipPercentage());
                    existingMember.setRole(member.getRole());
                    existingMember.setStatus(member.getStatus());
                    return memberRepository.save(existingMember);
                });
    }

    public void removeMember(Long groupId, Long memberId) {
        memberRepository.findById(memberId)
                .filter(m -> m.getGroup().getId().equals(groupId))
                .ifPresent(memberRepository::delete);
    }

    public Optional<GroupMember> getMemberByUserId(Long groupId, String userId) {
        return memberRepository.findByGroupIdAndUserId(groupId, userId);
    }
}
