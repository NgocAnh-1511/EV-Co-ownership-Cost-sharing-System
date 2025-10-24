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
        model.addAttribute("groups", groups);
        return "groups/list";
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
}