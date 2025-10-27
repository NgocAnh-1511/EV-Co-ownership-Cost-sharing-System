package com.example.ui.client;

import com.example.ui.dto.GroupDto;
import com.example.ui.dto.GroupSummaryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class GroupManagementClient {

    @Value("${group-management.service.url:http://localhost:8082}")
    private String groupManagementServiceUrl;

    private final RestTemplate restTemplate;

    public GroupManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GroupSummaryDto getGroupSummary() {
        try {
            String url = groupManagementServiceUrl + "/api/groups/summary";
            ResponseEntity<GroupSummaryDto> response = restTemplate.getForEntity(url, GroupSummaryDto.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Không thể kết nối đến Group Management Service: " + e.getMessage());
        }
    }

    public List<GroupDto> getAllGroups() {
        try {
            String url = groupManagementServiceUrl + "/api/groups";
            ResponseEntity<List<GroupDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<GroupDto>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Không thể lấy danh sách nhóm: " + e.getMessage());
        }
    }

    public GroupDto getGroupById(Long id) {
        try {
            String url = groupManagementServiceUrl + "/api/groups/" + id;
            ResponseEntity<GroupDto> response = restTemplate.getForEntity(url, GroupDto.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Không thể lấy thông tin nhóm: " + e.getMessage());
        }
    }

    public GroupDto createGroup(GroupDto group) {
        try {
            String url = groupManagementServiceUrl + "/api/groups";
            ResponseEntity<GroupDto> response = restTemplate.postForEntity(url, group, GroupDto.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo nhóm: " + e.getMessage());
        }
    }

    public GroupDto updateGroup(Long id, GroupDto group) {
        try {
            String url = groupManagementServiceUrl + "/api/groups/" + id;
            ResponseEntity<GroupDto> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(group),
                GroupDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật nhóm: " + e.getMessage());
        }
    }

    public void deleteGroup(Long id) {
        try {
            String url = groupManagementServiceUrl + "/api/groups/" + id;
            restTemplate.delete(url);
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa nhóm: " + e.getMessage());
        }
    }

    public List<GroupDto> searchGroups(String query) {
        try {
            String url = groupManagementServiceUrl + "/api/groups/search?q=" + query;
            ResponseEntity<List<GroupDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<GroupDto>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tìm kiếm nhóm: " + e.getMessage());
        }
    }

    public List<GroupDto> filterGroupsByStatus(String status) {
        try {
            String url = groupManagementServiceUrl + "/api/groups/filter?status=" + status;
            ResponseEntity<List<GroupDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<GroupDto>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Không thể lọc nhóm: " + e.getMessage());
        }
    }
}
