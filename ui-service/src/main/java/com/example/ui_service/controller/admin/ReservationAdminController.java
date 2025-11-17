package com.example.ui_service.controller.admin;

import com.example.ui_service.service.AdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReservationAdminController {

    private final AdminReservationService adminReservationService;

    @GetMapping("/reservations")
    public String manageReservations(Model model) {
        model.addAttribute("pageTitle", "Quản lý đặt lịch");
        model.addAttribute("pageSubtitle", "Quản lý và theo dõi các lịch đặt xe");
        
        try {
            // Gọi API để lấy danh sách reservations
            List<Map<String, Object>> reservations = adminReservationService.getAllReservations();
            model.addAttribute("reservations", reservations);
            System.out.println("✓ Đã load " + reservations.size() + " reservations vào model");
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi load reservations: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("reservations", List.of());
            model.addAttribute("errorMessage", "Không thể tải danh sách đặt lịch. Vui lòng thử lại sau.");
        }
        
        return "admin-reservations";
    }

    @GetMapping("/ai-recommendations")
    public String aiRecommendations(Model model) {
        model.addAttribute("pageTitle", "AI Recommendations");
        model.addAttribute("pageSubtitle", "Gợi ý thông minh từ AI để tối ưu hóa việc sử dụng xe và chia sẻ chi phí");
        model.addAttribute("message", "AI Recommendations - Gợi ý thông minh từ AI");
        return "admin-ai-recommendations";
    }

    @GetMapping("/schedule")
    public String adminSchedule(Model model) {
        model.addAttribute("pageTitle", "Quản lý lịch xe");
        model.addAttribute("pageSubtitle", "Theo dõi và quản lý lịch sử dụng xe đồng sở hữu");
        return "admin-schedule";
    }
}

