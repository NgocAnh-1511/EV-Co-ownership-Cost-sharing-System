package com.example.ui_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Collections;

@Controller
public class UIController {

    // --- Trang Public ---

    /**
     * 1. Trang Đăng nhập (Điểm vào chính)
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * 2. Trang Đăng ký
     */
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    /**
     * 3. Trang chủ (Redirect)
     * Chuyển hướng mặc định đến Login. Logic JS (login.js) sẽ xử lý
     * việc điều hướng dựa trên Role (Admin/User) nếu đã đăng nhập.
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // --- Trang ADMIN ---

    /**
     * 4. Trang Admin Dashboard (Quản lý Nhóm)
     */
    @GetMapping("/admin/groups")
    public String showGroupManagement(Model model) {
        model.addAttribute("pageTitle", "Quản Lí Nhóm Đồng Sở Hữu");
        model.addAttribute("currentPage", "groups"); // Dành cho sidebar admin

        // Dữ liệu mẫu (Dummy data)
        model.addAttribute("adminName", "Admin");
        model.addAttribute("totalGroups", 12);
        model.addAttribute("activeGroups", 8);
        model.addAttribute("brokenCars", 5);
        model.addAttribute("activeHosts", 10);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", 5);
        model.addAttribute("startIndex", 1);
        model.addAttribute("endIndex", 10);
        model.addAttribute("groups", Collections.emptyList());

        return "group-management";
    }

    /**
     * 5. Trang Admin Duyệt Hồ Sơ
     */
    @GetMapping("/admin/profile-approval")
    public String showProfileApprovalPage(Model model) {
        model.addAttribute("pageTitle", "Duyệt Hồ Sơ Người Dùng");
        model.addAttribute("currentPage", "approval"); // Dành cho sidebar admin
        return "admin/profile-approval";
    }

    /**
     * 6. Trang Admin Giám sát Tranh chấp
     */
    @GetMapping("/admin/disputes")
    public String showAdminDisputesPage(Model model) {
        model.addAttribute("pageTitle", "Giám sát Tranh chấp");
        model.addAttribute("currentPage", "disputes"); // Dành cho sidebar admin
        return "admin/admin-disputes"; // Trả về file /templates/admin/admin-disputes.html
    }


    // --- Trang USER ---

    /**
     * 7. Trang User Onboarding (Đăng ký hồ sơ)
     */
    @GetMapping("/user/onboarding")
    public String showUserOnboardingPage(Model model) {
        model.addAttribute("pageTitle", "Hoàn tất đăng ký");
        model.addAttribute("currentPage", "onboarding"); // Dành cho sidebar user
        return "user-onboarding";
    }

    /**
     * 8. Trang xem Tình trạng Hồ sơ (User)
     */
    @GetMapping("/user/profile-status")
    public String showProfileStatusPage(Model model) {
        model.addAttribute("pageTitle", "Tình trạng Hồ sơ");
        model.addAttribute("currentPage", "status"); // Dành cho sidebar user
        return "profile-status";
    }

    /**
     * 9. Trang Quản lý Hợp đồng (User)
     */
    @GetMapping("/user/contracts")
    public String showContractsPage(Model model) {
        model.addAttribute("pageTitle", "Quản lý Hợp đồng");
        model.addAttribute("currentPage", "contracts"); // Dành cho sidebar user
        return "user-contracts"; // Trả về file /templates/user-contracts.html
    }

<<<<<<< HEAD
    /**
     * 10. Trang Quản lý Tỷ lệ Sở hữu (User)
     */
    @GetMapping("/user/ownerships")
    public String showOwnershipsPage(Model model) {
        model.addAttribute("pageTitle", "Quản lý Tỷ lệ Sở hữu");
        model.addAttribute("currentPage", "ty-le"); // Dành cho sidebar user
        return "user-ownerships"; // Trả về file /templates/user-ownerships.html
    }
    @GetMapping("/user/account")
    public String showAccountManagementPage(Model model) {
        model.addAttribute("pageTitle", "Quản Lý Tài Khoản");
        model.addAttribute("currentPage", "account"); // Dành cho sidebar user
        return "user-account"; // Trả về file /templates/user-account.html
    }
}


=======
    @GetMapping("/admin/disputes")
    public String showAdminDisputesPage(Model model) {
        model.addAttribute("pageTitle", "Giám sát Tranh chấp");
        model.addAttribute("currentPage", "disputes"); // Dành cho sidebar admin
        return "admin/admin-disputes"; // Trả về file /templates/admin/admin-disputes.html
    }
}

>>>>>>> 17c2e87 (Lưu tạm thay đổi trước khi pull)
