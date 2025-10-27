package com.example.ui.controller;

import com.example.ui.dto.GroupSummaryDto;
import com.example.ui.dto.GroupDto;
import com.example.ui.service.GroupManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/groups")
public class GroupManagementController {

    @Autowired
    private GroupManagementService groupManagementService;

    @GetMapping("/management")
    public String groupManagement(Model model) {
        try {
            // Load summary data
            GroupSummaryDto summary = groupManagementService.getGroupSummary();
            model.addAttribute("summary", summary);

            // Load group list
            List<GroupDto> groups = groupManagementService.getAllGroups();
            model.addAttribute("groups", groups);

            return "groups/management";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu nhóm: " + e.getMessage());
            return "groups/management";
        }
    }

    @GetMapping("/create")
    public String createGroupForm(Model model) {
        model.addAttribute("group", new GroupDto());
        return "groups/create";
    }

    @PostMapping("/create")
    public String createGroup(@ModelAttribute GroupDto group, Model model) {
        try {
            GroupDto createdGroup = groupManagementService.createGroup(group);
            model.addAttribute("success", "Tạo nhóm thành công!");
            return "redirect:/groups/management";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tạo nhóm: " + e.getMessage());
            model.addAttribute("group", group);
            return "groups/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editGroupForm(@PathVariable Long id, Model model) {
        try {
            GroupDto group = groupManagementService.getGroupById(id);
            model.addAttribute("group", group);
            return "groups/edit";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin nhóm: " + e.getMessage());
            return "redirect:/groups/management";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateGroup(@PathVariable Long id, @ModelAttribute GroupDto group, Model model) {
        try {
            groupManagementService.updateGroup(id, group);
            model.addAttribute("success", "Cập nhật nhóm thành công!");
            return "redirect:/groups/management";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể cập nhật nhóm: " + e.getMessage());
            model.addAttribute("group", group);
            return "groups/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteGroup(@PathVariable Long id, Model model) {
        try {
            groupManagementService.deleteGroup(id);
            model.addAttribute("success", "Xóa nhóm thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Không thể xóa nhóm: " + e.getMessage());
        }
        return "redirect:/groups/management";
    }

    @GetMapping("/{id}/members")
    public String manageMembers(@PathVariable Long id, Model model) {
        try {
            GroupDto group = groupManagementService.getGroupById(id);
            model.addAttribute("group", group);
            return "groups/members";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin thành viên: " + e.getMessage());
            return "redirect:/groups/management";
        }
    }

    @GetMapping("/{id}/cars")
    public String manageCars(@PathVariable Long id, Model model) {
        try {
            GroupDto group = groupManagementService.getGroupById(id);
            model.addAttribute("group", group);
            return "groups/cars";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin xe: " + e.getMessage());
            return "redirect:/groups/management";
        }
    }

    @GetMapping("/search")
    @ResponseBody
    public List<GroupDto> searchGroups(@RequestParam String query) {
        try {
            return groupManagementService.searchGroups(query);
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/filter")
    @ResponseBody
    public List<GroupDto> filterGroups(@RequestParam String status) {
        try {
            return groupManagementService.filterGroupsByStatus(status);
        } catch (Exception e) {
            return List.of();
        }
    }
}
