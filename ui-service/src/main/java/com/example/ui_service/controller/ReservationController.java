package com.example.ui_service.controller;

import com.example.ui_service.service.VehicleService;
import com.example.ui_service.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final VehicleService vehicleService;
    private final ReservationService reservationService;

    public ReservationController(VehicleService vehicleService, ReservationService reservationService) {
        this.vehicleService = vehicleService;
        this.reservationService = reservationService;
    }

    // ✅ Trang đặt lịch mặc định: hiển thị xe đầu tiên
    @GetMapping("/book")
    public String showBookingForm(Model model, @RequestParam(value = "success", required = false) String success) {
        List<Map<String, Object>> vehicles = vehicleService.getVehicles();
        model.addAttribute("vehicles", vehicles);

        if (!vehicles.isEmpty()) {
            Long vehicleId = ((Number) vehicles.get(0).get("vehicleId")).longValue();
            model.addAttribute("selectedVehicleId", vehicleId);

            Map<String, Object> selectedVehicle = vehicles.stream()
                    .filter(v -> ((Number) v.get("vehicleId")).longValue() == vehicleId)
                    .findFirst()
                    .orElse(null);
            model.addAttribute("selectedVehicle", selectedVehicle);

            model.addAttribute("reservations", reservationService.getReservationsByVehicleId(vehicleId.intValue()));
        } else {
            model.addAttribute("reservations", List.of());
            model.addAttribute("selectedVehicleId", null);
            model.addAttribute("selectedVehicle", null);
        }

        return "booking-form";
    }

    // ✅ Khi chọn xe khác
    @GetMapping("/book/{vehicleId}")
    public String showBookingFormForVehicle(@PathVariable("vehicleId") Long vehicleId, Model model) {
        List<Map<String, Object>> vehicles = vehicleService.getVehicles();
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("selectedVehicleId", vehicleId);

        Map<String, Object> selectedVehicle = vehicles.stream()
                .filter(v -> ((Number) v.get("vehicleId")).longValue() == vehicleId)
                .findFirst()
                .orElse(null);
        model.addAttribute("selectedVehicle", selectedVehicle);

        model.addAttribute("reservations", reservationService.getReservationsByVehicleId(vehicleId.intValue()));
        
        // Flash attribute 'success' sẽ tự động được thêm vào model nếu có
        return "booking-form";
    }

    // ✅ Khi người dùng submit form đặt lịch
    @PostMapping("/book")
    public String createReservation(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "note", required = false) String note,
            @CookieValue(value = "userId", required = false) Long userIdFromCookie,
            @RequestParam(value = "userId", required = false) Long userIdFromForm,
            Model model
    ) {
        logger.info("🔥 POST /reservations/book - vehicleId={}, startDate={}, endDate={}", vehicleId, startDate, endDate);
        
        try {
            // Lấy userId theo thứ tự ưu tiên: form > cookie
            Long userId = userIdFromForm != null ? userIdFromForm : userIdFromCookie;
            logger.info("🔥 userId from form={}, from cookie={}, final={}", userIdFromForm, userIdFromCookie, userId);
            
            if (userId == null) {
                logger.warn("⚠️ No userId found, returning error");
                model.addAttribute("error", "❌ Vui lòng đăng nhập để đặt lịch");
                List<Map<String, Object>> vehicles = vehicleService.getVehicles();
                model.addAttribute("vehicles", vehicles);
                model.addAttribute("selectedVehicleId", vehicleId);
                model.addAttribute("reservations", reservationService.getReservationsByVehicleId(vehicleId.intValue()));
                return "booking-form";
            }

            // 🔹 Gửi body tới ReservationService (8081)
            Map<String, Object> newReservation = Map.of(
                    "vehicleId", vehicleId,
                    "userId", userId,
                    "startDate", startDate,
                    "endDate", endDate,
                    "note", note != null ? note : ""
            );

            reservationService.createReservation(newReservation);
            logger.info("✅ Reservation created successfully");

            // ✅ Thêm thông báo thành công (không redirect, hiện modal)
            model.addAttribute("showSuccessModal", true);
            model.addAttribute("successMessage", "Đặt lịch thành công!");
            logger.info("🔥 Added showSuccessModal=true to model");

            // ✅ Tải lại form với xe đã chọn
            List<Map<String, Object>> vehicles = vehicleService.getVehicles();
            model.addAttribute("vehicles", vehicles);
            model.addAttribute("selectedVehicleId", vehicleId);

            Map<String, Object> selectedVehicle = vehicles.stream()
                    .filter(v -> ((Number) v.get("vehicleId")).longValue() == vehicleId)
                    .findFirst()
                    .orElse(null);
            model.addAttribute("selectedVehicle", selectedVehicle);

            model.addAttribute("reservations", reservationService.getReservationsByVehicleId(vehicleId.intValue()));
            logger.info("🔥 Returning booking-form template");
            return "booking-form";

        } catch (Exception e) {
            model.addAttribute("error", "❌ Không thể đặt lịch: " + e.getMessage());

            // Tải lại form có lỗi
            List<Map<String, Object>> vehicles = vehicleService.getVehicles();
            model.addAttribute("vehicles", vehicles);
            model.addAttribute("selectedVehicleId", vehicleId);

            Map<String, Object> selectedVehicle = vehicles.stream()
                    .filter(v -> ((Number) v.get("vehicleId")).longValue() == vehicleId)
                    .findFirst()
                    .orElse(null);
            model.addAttribute("selectedVehicle", selectedVehicle);

            model.addAttribute("reservations", reservationService.getReservationsByVehicleId(vehicleId.intValue()));
            return "booking-form";
        }
    }
}
