package com.example.ui_service.client;

import com.example.ui_service.dto.GroupDto;
import com.example.ui_service.dto.GroupMemberDto;
import com.example.ui_service.dto.VoteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupManagementClient {

    private final RestTemplate restTemplate;

    @Value("${microservices.group-management.url}")
    private String groupManagementUrl;

    // Group Management APIs
    public List<GroupDto> getAllGroups() {
        ResponseEntity<List<GroupDto>> response = restTemplate.exchange(
                groupManagementUrl + "/api/groups",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<GroupDto>>() {}
        );
        return response.getBody();
    }

    public GroupDto getGroupById(Long id) {
        return restTemplate.getForObject(groupManagementUrl + "/api/groups/" + id, GroupDto.class);
    }

    public GroupDto createGroup(GroupDto group) {
        return restTemplate.postForObject(groupManagementUrl + "/api/groups", group, GroupDto.class);
    }

    public GroupDto updateGroup(Long id, GroupDto group) {
        restTemplate.put(groupManagementUrl + "/api/groups/" + id, group);
        return getGroupById(id);
    }

    public void deleteGroup(Long id) {
        restTemplate.delete(groupManagementUrl + "/api/groups/" + id);
    }

    // Member Management APIs
    public List<GroupMemberDto> getGroupMembers(Long groupId) {
        ResponseEntity<List<GroupMemberDto>> response = restTemplate.exchange(
                groupManagementUrl + "/api/groups/" + groupId + "/members",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<GroupMemberDto>>() {}
        );
        return response.getBody();
    }

    public GroupMemberDto addMember(Long groupId, GroupMemberDto member) {
        return restTemplate.postForObject(
                groupManagementUrl + "/api/groups/" + groupId + "/members",
                member,
                GroupMemberDto.class
        );
    }

    public GroupMemberDto updateMember(Long groupId, Long memberId, GroupMemberDto member) {
        restTemplate.put(groupManagementUrl + "/api/groups/" + groupId + "/members/" + memberId, member);
        return restTemplate.getForObject(
                groupManagementUrl + "/api/groups/" + groupId + "/members/" + memberId,
                GroupMemberDto.class
        );
    }

    public void removeMember(Long groupId, Long memberId) {
        restTemplate.delete(groupManagementUrl + "/api/groups/" + groupId + "/members/" + memberId);
    }

    // Vote Management APIs
    public List<VoteDto> getVotesByGroup(Long groupId) {
        ResponseEntity<List<VoteDto>> response = restTemplate.exchange(
                groupManagementUrl + "/api/votes/group/" + groupId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VoteDto>>() {}
        );
        return response.getBody();
    }

    public VoteDto createVote(VoteDto vote) {
        return restTemplate.postForObject(groupManagementUrl + "/api/votes", vote, VoteDto.class);
    }

    public VoteDto getVoteById(Long id) {
        return restTemplate.getForObject(groupManagementUrl + "/api/votes/" + id, VoteDto.class);
    }
}
