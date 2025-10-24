package com.example.ui_service.client;

import com.example.ui_service.dto.GroupDto;
import com.example.ui_service.dto.GroupMemberDto;
import com.example.ui_service.dto.VoteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class GroupManagementClient {

    @Value("${microservices.group-management.url:http://localhost:8082}")
    private String groupManagementUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<GroupDto> getAllGroups() {
        try {
            GroupDto[] groups = restTemplate.getForObject(groupManagementUrl + "/api/groups", GroupDto[].class);
            return groups != null ? Arrays.asList(groups) : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching groups: " + e.getMessage());
            return List.of();
        }
    }

    public GroupDto createGroup(GroupDto groupDto) {
        try {
            return restTemplate.postForObject(groupManagementUrl + "/api/groups", groupDto, GroupDto.class);
        } catch (Exception e) {
            System.err.println("Error creating group: " + e.getMessage());
            return null;
        }
    }

    public List<GroupMemberDto> getGroupMembers(Integer groupId) {
        try {
            GroupMemberDto[] members = restTemplate.getForObject(groupManagementUrl + "/api/groups/" + groupId + "/members", GroupMemberDto[].class);
            return members != null ? Arrays.asList(members) : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching group members: " + e.getMessage());
            return List.of();
        }
    }

    public GroupMemberDto addGroupMember(Integer groupId, GroupMemberDto memberDto) {
        try {
            return restTemplate.postForObject(groupManagementUrl + "/api/groups/" + groupId + "/members", memberDto, GroupMemberDto.class);
        } catch (Exception e) {
            System.err.println("Error adding group member: " + e.getMessage());
            return null;
        }
    }

    public List<VoteDto> getGroupVotes(Integer groupId) {
        try {
            VoteDto[] votes = restTemplate.getForObject(groupManagementUrl + "/api/groups/" + groupId + "/votes", VoteDto[].class);
            return votes != null ? Arrays.asList(votes) : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching group votes: " + e.getMessage());
            return List.of();
        }
    }

    public VoteDto createVote(Integer groupId, VoteDto voteDto) {
        try {
            return restTemplate.postForObject(groupManagementUrl + "/api/groups/" + groupId + "/votes", voteDto, VoteDto.class);
        } catch (Exception e) {
            System.err.println("Error creating vote: " + e.getMessage());
            return null;
        }
    }
}