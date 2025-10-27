package com.example.ui.service;

import com.example.ui.dto.GroupDto;
import com.example.ui.dto.GroupSummaryDto;
import com.example.ui.client.GroupManagementClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupManagementService {

    @Autowired
    private GroupManagementClient groupManagementClient;

    public GroupSummaryDto getGroupSummary() {
        try {
            // Call group management service to get summary data
            return groupManagementClient.getGroupSummary();
        } catch (Exception e) {
            // Return mock data if service is unavailable
            return new GroupSummaryDto(12, 48, 25, 10);
        }
    }

    public List<GroupDto> getAllGroups() {
        try {
            return groupManagementClient.getAllGroups();
        } catch (Exception e) {
            // Return mock data if service is unavailable
            return getMockGroups();
        }
    }

    public GroupDto getGroupById(Long id) {
        try {
            return groupManagementClient.getGroupById(id);
        } catch (Exception e) {
            throw new RuntimeException("Không thể tải thông tin nhóm: " + e.getMessage());
        }
    }

    public GroupDto createGroup(GroupDto group) {
        try {
            return groupManagementClient.createGroup(group);
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo nhóm: " + e.getMessage());
        }
    }

    public GroupDto updateGroup(Long id, GroupDto group) {
        try {
            return groupManagementClient.updateGroup(id, group);
        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật nhóm: " + e.getMessage());
        }
    }

    public void deleteGroup(Long id) {
        try {
            groupManagementClient.deleteGroup(id);
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa nhóm: " + e.getMessage());
        }
    }

    public List<GroupDto> searchGroups(String query) {
        try {
            return groupManagementClient.searchGroups(query);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<GroupDto> filterGroupsByStatus(String status) {
        try {
            return groupManagementClient.filterGroupsByStatus(status);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<GroupDto> getMockGroups() {
        return List.of(
            new GroupDto(1L, "Nhóm Gia Đình Nguyễn", "Nhóm sở hữu xe gia đình", "Hoạt động"),
            new GroupDto(2L, "Nhóm Bạn Thân Hà Nội", "Nhóm bạn bè chia sẻ xe", "Hoạt động"),
            new GroupDto(3L, "Nhóm Công Ty ABC", "Nhóm xe công ty", "Tạm dừng")
        );
    }
}
