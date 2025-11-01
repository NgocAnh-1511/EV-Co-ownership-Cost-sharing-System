package com.example.ui_service.controller;

import com.example.ui_service.model.VehiclegroupDTO;
import com.example.ui_service.service.VehicleGroupRestClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class VehicleGroupController {

    private final VehicleGroupRestClient vehicleGroupRestClient;

    public VehicleGroupController(VehicleGroupRestClient vehicleGroupRestClient) {
        this.vehicleGroupRestClient = vehicleGroupRestClient;
    }

    @GetMapping("/admin/staff-management")
    public String StaffMangement(Model model,
                                 @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
                                 @RequestParam(value = "statusFilter", required = false, defaultValue = "Tất cả") String statusFilter) {

        // Lấy danh sách tất cả các nhóm xe từ API
        List<VehiclegroupDTO> vehicleGroups = vehicleGroupRestClient.getAllVehicleGroups();

        // Lọc nhóm theo tên (searchQuery) và trạng thái (statusFilter)
        List<VehiclegroupDTO> filteredGroups = vehicleGroups.stream()
                .filter(group -> (searchQuery.isEmpty() || group.getName().toLowerCase().contains(searchQuery.toLowerCase())) &&
                        ("Tất cả".equals(statusFilter) || group.getActive().equalsIgnoreCase(statusFilter)))
                .collect(Collectors.toList());

        // Tính tổng số nhóm và nhóm đang hoạt động
        int totalGroups = filteredGroups.size();
        int activeGroups = (int) filteredGroups.stream()
                .filter(group -> "active".equalsIgnoreCase(group.getActive()))  // Kiểm tra nếu active = "active"
                .count();

        // Thêm dữ liệu vào model

        model.addAttribute("filteredGroups", filteredGroups);  // Danh sách nhóm đã lọc
        model.addAttribute("totalGroups", totalGroups);         // Tổng số nhóm
        model.addAttribute("activeGroups", activeGroups);       // Số nhóm đang hoạt động
        model.addAttribute("groupFilter", statusFilter);        // Trạng thái filter
        model.addAttribute("searchQuery", searchQuery);         // Từ khóa tìm kiếm

        // Lưu trạng thái lọc vào model
        model.addAttribute("statusFilter", statusFilter);       // Trạng thái lọc đã chọn

        // Nếu không có kết quả, thêm một thông báo
        if (filteredGroups.isEmpty()) {
            model.addAttribute("noDataMessage", "Không tìm thấy nhóm xe phù hợp.");
        }
        vehicleGroups.forEach(group -> System.out.println("Group Name: " + group.getName() + ", Active: " + group.getActive()));

        return "admin/staff-management";  // Trả về trang staff-management
    }
}
