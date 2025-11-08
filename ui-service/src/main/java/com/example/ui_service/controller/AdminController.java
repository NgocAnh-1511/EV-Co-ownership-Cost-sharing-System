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
     * Admin Dashboard - Show all reservations with full CRUD
     * Handles both /admin and /admin/reservations
     */
    @GetMapping({"", "/reservations"})
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
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    /**
     * Update reservation
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
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    /**
     * ====================================================================
     * CẬP NHẬT TRẠNG THÁI RESERVATION TỪ ADMIN PANEL
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Controller xử lý request cập nhật trạng thái reservation từ admin panel
     * - Gọi Reservation Service API để cập nhật trạng thái
     * - Reservation Service sẽ tự động cập nhật từ cả 2 bảng:
     *   + co_ownership_booking.reservations (bảng chính) - CẬP NHẬT TRƯỚC
     *   + co_ownership_admin.reservations (bảng admin) - CẬP NHẬT SAU
     * 
     * QUY TRÌNH:
     * 1. Admin ấn nút "Đổi trạng thái" trên admin panel
     * 2. Frontend gửi POST request đến endpoint này
     * 3. Controller gọi Reservation Service API (PUT /api/reservations/{id}/status?status=XXX)
     * 4. Reservation Service cập nhật từ bảng chính TRƯỚC, sau đó cập nhật bảng admin
     * 5. Trả về thông báo thành công/thất bại cho admin
     * 
     * @param id ID của reservation cần cập nhật trạng thái
     * @param status Trạng thái mới (BOOKED, COMPLETED, CANCELLED)
     * @param redirectAttributes Để truyền thông báo thành công/thất bại
     * @return Redirect về trang admin dashboard
     */
    @PostMapping("/reservations/{id}/status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Tạo URL endpoint của Reservation Service
            // Reservation Service sẽ cập nhật từ bảng chính TRƯỚC, sau đó cập nhật bảng admin
            String url = reservationServiceUrl + "/api/reservations/" + id + "/status?status=" + status;
            logger.info("🔄 [ADMIN STATUS UPDATE] Bắt đầu cập nhật trạng thái reservation ID: {} → {} từ admin panel", id, status);

            // Gọi PUT API đến Reservation Service
            // Reservation Service sẽ tự động cập nhật từ cả 2 bảng database
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.PUT, entity, new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

            // Kiểm tra status code của response
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [ADMIN STATUS UPDATE SUCCESS] Đã cập nhật thành công trạng thái reservation ID: {} → {}", id, status);
                redirectAttributes.addFlashAttribute("successMessage", "✅ Đổi trạng thái thành công!");
            } else {
                logger.warn("⚠️ [ADMIN STATUS UPDATE WARNING] Update trả về status code không thành công: {}", response.getStatusCode());
                redirectAttributes.addFlashAttribute("errorMessage", "❌ Không thể đổi trạng thái. Status: " + response.getStatusCode());
            }

            return "redirect:/admin";
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Lỗi HTTP 4xx (Bad Request, Not Found, etc.)
            logger.error("❌ [ADMIN STATUS UPDATE ERROR] HTTP client error khi cập nhật trạng thái reservation {}: {}", id, e.getMessage());
            logger.error("Response body: {}", e.getResponseBodyAsString());
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi khi đổi trạng thái: " + e.getStatusCode() + " - " + e.getMessage());
            return "redirect:/admin";
            
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // Lỗi HTTP 5xx (Internal Server Error, etc.)
            logger.error("❌ [ADMIN STATUS UPDATE ERROR] HTTP server error khi cập nhật trạng thái reservation {}: {}", id, e.getMessage());
            logger.error("Response body: {}", e.getResponseBodyAsString());
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi server khi đổi trạng thái: " + e.getMessage());
            return "redirect:/admin";
            
        } catch (Exception e) {
            // Lỗi không xác định
            logger.error("❌ [ADMIN STATUS UPDATE ERROR] Lỗi không xác định khi cập nhật trạng thái reservation {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    /**
     * ====================================================================
     * XÓA RESERVATION TỪ ADMIN PANEL
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Controller xử lý request xóa reservation từ admin panel
     * - Gọi Reservation Service API để xóa reservation
     * - Reservation Service sẽ tự động xóa từ cả 2 bảng:
     *   + co_ownership_booking.reservations (bảng chính)
     *   + co_ownership_admin.reservations (bảng admin)
     * 
     * QUY TRÌNH:
     * 1. Admin ấn nút "Xóa" trên admin panel
     * 2. Frontend gửi POST request đến endpoint này
     * 3. Controller gọi Reservation Service API (DELETE /api/reservations/{id})
     * 4. Reservation Service xóa từ cả 2 bảng database
     * 5. Trả về thông báo thành công/thất bại cho admin
     * 
     * @param id ID của reservation cần xóa
     * @param redirectAttributes Để truyền thông báo thành công/thất bại
     * @return Redirect về trang admin dashboard
     */
    @PostMapping("/reservations/{id}/delete")
    public String deleteReservation(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Tạo URL endpoint của Reservation Service
            String url = reservationServiceUrl + "/api/reservations/" + id;
            logger.info("🗑️ [ADMIN DELETE] Bắt đầu xóa reservation ID: {} từ admin panel", id);
            
            // Gọi DELETE API đến Reservation Service
            // Reservation Service sẽ tự động xóa từ cả 2 bảng database
            ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                null,
                Void.class
            );
            
            // Kiểm tra status code của response
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [ADMIN DELETE SUCCESS] Đã xóa thành công reservation ID: {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "✅ Đã xóa lịch thành công!");
            } else {
                logger.warn("⚠️ [ADMIN DELETE WARNING] Delete trả về status code không thành công: {}", response.getStatusCode());
                redirectAttributes.addFlashAttribute("errorMessage", "❌ Không thể xóa lịch. Status: " + response.getStatusCode());
            }
            
            return "redirect:/admin";
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Lỗi HTTP 4xx (Bad Request, Not Found, etc.)
            logger.error("❌ [ADMIN DELETE ERROR] HTTP client error khi xóa reservation {}: {}", id, e.getMessage());
            logger.error("Response body: {}", e.getResponseBodyAsString());
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi khi xóa lịch: " + e.getStatusCode() + " - " + e.getMessage());
            return "redirect:/admin";
            
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // Lỗi HTTP 5xx (Internal Server Error, etc.)
            logger.error("❌ [ADMIN DELETE ERROR] HTTP server error khi xóa reservation {}: {}", id, e.getMessage());
            logger.error("Response body: {}", e.getResponseBodyAsString());
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi server khi xóa lịch: " + e.getMessage());
            return "redirect:/admin";
            
        } catch (Exception e) {
            // Lỗi không xác định
            logger.error("❌ [ADMIN DELETE ERROR] Lỗi không xác định khi xóa reservation {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            return "redirect:/admin";
        }
    }
}

