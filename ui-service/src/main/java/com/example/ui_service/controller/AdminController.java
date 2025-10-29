package com.example.ui_service.controller;

import com.example.ui_service.model.User; // Import User
import com.example.ui_service.service.UserService; // Import UserService
import org.springframework.beans.factory.annotation.Autowired; // Import Autowired
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Import Model
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Import PathVariable
import org.springframework.web.bind.annotation.PostMapping; // Import PostMapping
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import RedirectAttributes

import java.util.List; // Import List

@Controller
@RequestMapping("/admin") // Mọi URL trong controller này bắt đầu bằng /admin
public class AdminController {

    @Autowired // Tự động inject UserService
    private UserService userService;

    // Trang Dashboard Admin cơ bản
    @GetMapping("/dashboard")
    public String showAdminDashboard() {
        return "admin/dashboard"; // Trả về templates/admin/dashboard.html
    }

    // Trang Quản lý Người dùng (MỚI)
    @GetMapping("/users")
    public String showUserManagement(Model model) {
        List<User> users = userService.findAllUsers(); // Lấy danh sách user
        model.addAttribute("users", users); // Gửi danh sách user sang view
        return "admin/user-management"; // Trả về templates/admin/user-management.html
    }

    // Endpoint xử lý việc nâng cấp Role (MỚI)
    // Ví dụ: POST /admin/users/promote/123
    @PostMapping("/users/promote/{userId}")
    public String promoteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.promoteUserToAdmin(userId); // Gọi service để nâng cấp
            // Gửi thông báo thành công về trang user list
            redirectAttributes.addFlashAttribute("successMessage", "Đã nâng cấp người dùng thành Admin!");
        } catch (Exception e) {
            // Gửi thông báo lỗi về trang user list
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users"; // Chuyển hướng về lại trang danh sách user
    }
}