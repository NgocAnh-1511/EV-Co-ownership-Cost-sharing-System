package com.example.ui_service.controller;

import com.example.ui_service.client.GroupManagementClient;
import com.example.ui_service.dto.GroupDto;
import com.example.ui_service.dto.GroupMemberDto;
import com.example.ui_service.dto.VoteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupManagementClient groupManagementClient;

    @GetMapping
    public String getAllGroups(Model model) {
        try {
            List<GroupDto> groups = groupManagementClient.getAllGroups();
            model.addAttribute("groups", groups);
            model.addAttribute("success", "Groups loaded successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load groups: " + e.getMessage());
            model.addAttribute("groups", List.of());
        }
        return "groups/list";
    }

    @GetMapping("/{id}")
    public String getGroupById(@PathVariable Long id, Model model) {
        try {
            GroupDto group = groupManagementClient.getGroupById(id);
            List<GroupMemberDto> members = groupManagementClient.getGroupMembers(id);
            List<VoteDto> votes = groupManagementClient.getVotesByGroup(id);
            
            model.addAttribute("group", group);
            model.addAttribute("members", members);
            model.addAttribute("votes", votes);
            model.addAttribute("success", "Group details loaded successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load group details: " + e.getMessage());
        }
        return "groups/detail";
    }

    @GetMapping("/create")
    public String createGroupForm(Model model) {
        model.addAttribute("group", new GroupDto());
        return "groups/create";
    }

    @PostMapping("/create")
    public String createGroup(@ModelAttribute GroupDto group, Model model) {
        try {
            GroupDto createdGroup = groupManagementClient.createGroup(group);
            model.addAttribute("success", "Group created successfully");
            return "redirect:/groups/" + createdGroup.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create group: " + e.getMessage());
            model.addAttribute("group", group);
            return "groups/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editGroupForm(@PathVariable Long id, Model model) {
        try {
            GroupDto group = groupManagementClient.getGroupById(id);
            model.addAttribute("group", group);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load group: " + e.getMessage());
            return "redirect:/groups";
        }
        return "groups/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateGroup(@PathVariable Long id, @ModelAttribute GroupDto group, Model model) {
        try {
            groupManagementClient.updateGroup(id, group);
            model.addAttribute("success", "Group updated successfully");
            return "redirect:/groups/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update group: " + e.getMessage());
            model.addAttribute("group", group);
            return "groups/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteGroup(@PathVariable Long id, Model model) {
        try {
            groupManagementClient.deleteGroup(id);
            model.addAttribute("success", "Group deleted successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete group: " + e.getMessage());
        }
        return "redirect:/groups";
    }

    @GetMapping("/{groupId}/members/add")
    public String addMemberForm(@PathVariable Long groupId, Model model) {
        model.addAttribute("member", new GroupMemberDto());
        model.addAttribute("groupId", groupId);
        return "groups/add-member";
    }

    @PostMapping("/{groupId}/members/add")
    public String addMember(@PathVariable Long groupId, @ModelAttribute GroupMemberDto member, Model model) {
        try {
            member.setGroupId(groupId);
            groupManagementClient.addMember(groupId, member);
            model.addAttribute("success", "Member added successfully");
            return "redirect:/groups/" + groupId;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add member: " + e.getMessage());
            model.addAttribute("member", member);
            model.addAttribute("groupId", groupId);
            return "groups/add-member";
        }
    }

    @PostMapping("/{groupId}/members/{memberId}/remove")
    public String removeMember(@PathVariable Long groupId, @PathVariable Long memberId, Model model) {
        try {
            groupManagementClient.removeMember(groupId, memberId);
            model.addAttribute("success", "Member removed successfully");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to remove member: " + e.getMessage());
        }
        return "redirect:/groups/" + groupId;
    }

    @GetMapping("/{groupId}/votes/create")
    public String createVoteForm(@PathVariable Long groupId, Model model) {
        model.addAttribute("vote", new VoteDto());
        model.addAttribute("groupId", groupId);
        return "groups/create-vote";
    }

    @PostMapping("/{groupId}/votes/create")
    public String createVote(@PathVariable Long groupId, @ModelAttribute VoteDto vote, Model model) {
        try {
            vote.setGroupId(groupId);
            groupManagementClient.createVote(vote);
            model.addAttribute("success", "Vote created successfully");
            return "redirect:/groups/" + groupId;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create vote: " + e.getMessage());
            model.addAttribute("vote", vote);
            model.addAttribute("groupId", groupId);
            return "groups/create-vote";
        }
    }
}
