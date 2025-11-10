package com.example.ui_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Value("${reservation.service.url}")
    private String reservationServiceUrl;
    
    @Value("${reservation.admin.service.url:http://localhost:8082}")
    private String reservationAdminServiceUrl;

    @Value("${vehicle.service.url}")
    private String vehicleServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Block /admin access - return 404
     * Only /admin/reservations is allowed
     */
    @GetMapping("")
    public void blockAdminAccess(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found. Please use /admin/reservations");
    }

    /**
     * Admin Dashboard - Show all reservations with full CRUD
     * Only handles /admin/reservations
     */
    @GetMapping("/reservations")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String adminDashboard(Model model) {
        try {
            // Fetch all reservations from Admin Service (has user & vehicle info)
            String url = reservationAdminServiceUrl + "/api/admin/reservations";
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    List.class
            );

            List<Map<String, Object>> reservations = (List<Map<String, Object>>) response.getBody();
            if (reservations == null) {
                reservations = new ArrayList<>();
            }

            // Fetch all vehicles for dropdown
            String vehicleUrl = vehicleServiceUrl + "/api/vehicles";
            ResponseEntity<List> vehicleResponse = restTemplate.exchange(
                    vehicleUrl,
                    HttpMethod.GET,
                    null,
                    List.class
            );
            List<Map<String, Object>> vehicles = (List<Map<String, Object>>) vehicleResponse.getBody();

            // Fetch all users for dropdown
            String userUrl = userServiceUrl + "/api/users";
            ResponseEntity<List> userResponse = restTemplate.exchange(
                    userUrl,
                    HttpMethod.GET,
                    null,
                    List.class
            );
            List<Map<String, Object>> users = (List<Map<String, Object>>) userResponse.getBody();

            model.addAttribute("reservations", reservations);
            model.addAttribute("vehicles", vehicles != null ? vehicles : new ArrayList<>());
            model.addAttribute("users", users != null ? users : new ArrayList<>());
            model.addAttribute("statuses", Arrays.asList("BOOKED", "COMPLETED", "CANCELLED"));

            return "admin-schedule";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu: " + e.getMessage());
            model.addAttribute("reservations", new ArrayList<>());
            model.addAttribute("vehicles", new ArrayList<>());
            model.addAttribute("users", new ArrayList<>());
            model.addAttribute("statuses", Arrays.asList("BOOKED", "COMPLETED", "CANCELLED"));
            return "admin-schedule";
        }
    }

    /**
     * Create new reservation (Admin)
     */
    @PostMapping("/reservations/create")
    public String createReservation(
            @RequestParam Long userId,
            @RequestParam Long vehicleId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String url = reservationServiceUrl + "/api/reservations";

            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("vehicleId", vehicleId);
            request.put("startDatetime", startDate);
            request.put("endDatetime", endDate);
            request.put("purpose", note);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(url, entity, Map.class);

            redirectAttributes.addFlashAttribute("successMessage", "✅ Tạo lịch đặt thành công!");
            return "redirect:/admin/reservations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            return "redirect:/admin/reservations";
        }
    }

    /**
     * Cập nhật reservation
     */
    @PostMapping("/reservations/{id}/update")
    public String updateReservation(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam Long vehicleId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String note,
            @RequestParam String status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String url = reservationServiceUrl + "/api/reservations/" + id;
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("vehicleId", vehicleId);
            request.put("startDatetime", startDate);
            request.put("endDatetime", endDate);
            request.put("purpose", note);
            request.put("status", status);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);

            redirectAttributes.addFlashAttribute("successMessage", "✅ Cập nhật thành công!");
            return "redirect:/admin/reservations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            return "redirect:/admin/reservations";
        }
    }
    
    /**
     * Cập nhật trạng thái reservation
     */
    @PostMapping("/reservations/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String url = reservationServiceUrl + "/api/reservations/" + id + "/status?status=" + status;
            restTemplate.exchange(url, HttpMethod.PUT, null, Map.class);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Đã cập nhật trạng thái!");
            return "redirect:/admin/reservations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            return "redirect:/admin/reservations";
        }
    }


    /**
     * Xóa reservation
     */
    @PostMapping("/reservations/{id}/delete")
    public String deleteReservation(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String url = reservationServiceUrl + "/api/reservations/" + id;
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Đã xóa thành công!");
            return "redirect:/admin/reservations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            return "redirect:/admin/reservations";
        }
    }
}

