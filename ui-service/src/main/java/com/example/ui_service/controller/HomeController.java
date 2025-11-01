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
public class HomeController {

    private final VehicleGroupRestClient vehicleGroupRestClient;

    public HomeController(VehicleGroupRestClient vehicleGroupRestClient) {
        this.vehicleGroupRestClient = vehicleGroupRestClient;
    }

    @GetMapping("/admin/staff-management")
    public String StaffMangement(Model model,
                                 @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
                                 @RequestParam(value = "statusFilter", required = false, defaultValue = "Tất cả") String statusFilter) {

        // Thiết lập các tham số cho trang
        model.addAttribute("pageTitle", "Quản Lý Nhóm Xe Đồng Sở Hữu");
        model.addAttribute("pageDescription", "Quản lý thông tin nhóm xe");

        // Lấy danh sách tất cả các nhóm xe từ API
        List<VehiclegroupDTO> vehicleGroups = vehicleGroupRestClient.getAllVehicleGroups();

        // Lọc nhóm theo tên (searchQuery) và trạng thái (statusFilter)
        List<VehiclegroupDTO> filteredGroups = vehicleGroups.stream()
                .filter(group -> (searchQuery.isEmpty() || group.getGroupName().toLowerCase().contains(searchQuery.toLowerCase())) &&
                        (statusFilter.equals("Tất cả") || group.getActive().equals(statusFilter)))
                .collect(Collectors.toList());

        // Tính tổng số nhóm và nhóm đang hoạt động
        int totalGroups = filteredGroups.size();
        int activeGroups = (int) filteredGroups.stream()
                .filter(group -> "active".equals(group.getActive()))  // Kiểm tra nếu active = "active"
                .count();

        // Thêm dữ liệu vào model
        model.addAttribute("filteredGroups", filteredGroups);  // Danh sách nhóm đã lọc
        model.addAttribute("totalGroups", totalGroups);         // Tổng số nhóm
        model.addAttribute("activeGroups", activeGroups);       // Số nhóm đang hoạt động
        model.addAttribute("groupFilter", statusFilter);        // Trạng thái filter
        model.addAttribute("searchQuery", searchQuery);         // Từ khóa tìm kiếm

        return "admin/staff-management";  // Trả về trang staff-management
    }


    @GetMapping ("/admin/vehicle-manager")
    public String VehicleManager(Model model) {
        model.addAttribute("pageTitle", "Quản Lý Các Dịch Vụ Xe");
        model.addAttribute("pageDescription", "Quản Lý Danh Sách xe theo trạng thái");
        return "admin/vehicle-manager";}

    
    @GetMapping("/admin/enhanced-contract")
    public String EnhancedContractManagement(Model model) {
        model.addAttribute("pageTitle", "Quản Lý Hợp Đồng Điện Tử");
        model.addAttribute("pageDescription", "Quản lý hợp đồng pháp lý cho nhóm đồng sở hữu");
        return "admin/enhanced-contract-management";
    }
}
