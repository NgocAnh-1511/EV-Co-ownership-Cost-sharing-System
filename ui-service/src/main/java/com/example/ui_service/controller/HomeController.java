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
