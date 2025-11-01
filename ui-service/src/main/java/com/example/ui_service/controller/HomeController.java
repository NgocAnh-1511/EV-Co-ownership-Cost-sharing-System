package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/admin/staff-management")
    public String StaffMangement(Model model) {
        model.addAttribute("pageTitle", "Quản Lý Nhóm Đồng Sở Hữu");
        model.addAttribute("pageDescription", "Quản lý thông tin nhóm và xe của bạn");
        return "admin/staff-management";
    }
    @GetMapping ("/admin/vehicle-manager")
    public String VehicleManager() {return "vehicle-manager";}

    
    @GetMapping("/admin/vehicle-group")
    public String VehicleGroupManagement() {
        return "vehicle-group-management";
    }
    
    @GetMapping("/admin/enhanced-contract")
    public String EnhancedContractManagement() {
        return "enhanced-contract-management";
    }
}
