package com.example.ui_service.controller;

import com.example.ui_service.client.GroupManagementClient;
import com.example.ui_service.dto.GroupDto;
import com.example.ui_service.dto.GroupMemberDto;
import com.example.ui_service.dto.VoteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupManagementClient groupManagementClient;

    @GetMapping
    public String listGroups(Model model) {
        List<GroupDto> groups = groupManagementClient.getAllGroups();
        
        // If no groups from service, create sample data for testing
        if (groups.isEmpty()) {
            groups = createSampleGroups();
        }
        
        model.addAttribute("groups", groups);
        
        // Calculate total members
        int totalMembers = groups.stream()
                .mapToInt(group -> group.getMemberCount() != null ? group.getMemberCount() : 0)
                .sum();
        model.addAttribute("totalMembers", totalMembers);
        
        return "groups/list";
    }
    
    private List<GroupDto> createSampleGroups() {
        return List.of(
            new GroupDto(1, "Nhóm Tesla Model 3", 1, 1, "Active", 
                        new java.util.Date(), 4, 15000000.0, 2),
            new GroupDto(2, "Nhóm BMW i3", 2, 2, "Active", 
                        new java.util.Date(), 3, 12000000.0, 1),
            new GroupDto(3, "Nhóm Nissan Leaf", 3, 3, "Inactive", 
                        new java.util.Date(), 2, 8000000.0, 0),
            new GroupDto(4, "Nhóm Hyundai Ioniq", 4, 4, "Active", 
                        new java.util.Date(), 5, 18000000.0, 3)
        );
    }

    @GetMapping("/create")
    public String createGroupForm(Model model) {
        model.addAttribute("group", new GroupDto());
        return "groups/create";
    }

    @PostMapping("/create")
    public String createGroup(@ModelAttribute GroupDto groupDto) {
        groupManagementClient.createGroup(groupDto);
        return "redirect:/groups";
    }

    @GetMapping("/{id}/members")
    public String listGroupMembers(@PathVariable Integer id, Model model) {
        List<GroupMemberDto> members = groupManagementClient.getGroupMembers(id);
        model.addAttribute("members", members);
        model.addAttribute("groupId", id);
        return "groups/members";
    }

    @PostMapping("/{id}/members")
    public String addGroupMember(@PathVariable Integer id, @ModelAttribute GroupMemberDto memberDto) {
        groupManagementClient.addGroupMember(id, memberDto);
        return "redirect:/groups/" + id + "/members";
    }

    @GetMapping("/{id}/votes")
    public String listGroupVotes(@PathVariable Integer id, Model model) {
        List<VoteDto> votes = groupManagementClient.getGroupVotes(id);
        model.addAttribute("votes", votes);
        model.addAttribute("groupId", id);
        return "groups/votes";
    }

    @PostMapping("/{id}/votes")
    public String createVote(@PathVariable Integer id, @ModelAttribute VoteDto voteDto) {
        groupManagementClient.createVote(id, voteDto);
        return "redirect:/groups/" + id + "/votes";
    }

    @GetMapping("/api/members")
    @ResponseBody
    public List<GroupMemberDto> getMembersApi() {
        // Get members from the first group for demo purposes
        // In a real application, you might want to get members from a specific group
        List<GroupDto> groups = groupManagementClient.getAllGroups();
        if (!groups.isEmpty()) {
            return groupManagementClient.getGroupMembers(groups.get(0).getGroupId());
        }
        return List.of();
    }
    
    @GetMapping("/voting")
    public String voting(Model model) {
        return "groups/voting";
    }
    
    @GetMapping("/fund")
    public String fund(Model model) {
        return "groups/fund";
    }
}