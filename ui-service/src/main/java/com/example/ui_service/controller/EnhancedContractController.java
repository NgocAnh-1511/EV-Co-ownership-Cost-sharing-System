package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EnhancedContractController {

    /**
     * Trang quản lý hợp đồng điện tử
     * @param model Model để truyền dữ liệu vào view
     * @return Tên template view
     */
    @GetMapping("/admin/enhanced-contract")
    public String enhancedContractManagement(Model model) {
        model.addAttribute("pageTitle", "Quản Lý Hợp Đồng Điện Tử");
        model.addAttribute("pageDescription", "Quản lý hợp đồng pháp lý cho nhóm đồng sở hữu");
        return "admin/enhanced-contract-management";
    }
}






