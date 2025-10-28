package com.example.ui_service.controller;

import com.example.ui_service.service.VehicleService;
import com.example.ui_service.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final VehicleService vehicleService;
    private final ReservationService reservationService;

    public ReservationController(VehicleService vehicleService, ReservationService reservationService) {
        this.vehicleService = vehicleService;
        this.reservationService = reservationService;
    }

    // ✅ Trang đặt lịch mặc định: hiển thị xe đầu tiên
    @GetMapping("/book")
    public String showBookingForm(Model model) {
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
        return "booking-form";
    }

    // ✅ Khi người dùng submit form đặt lịch
    @PostMapping("/book")
    public String createReservation(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "note", required = false) String note,
            Model model
    ) {
        try {
            // 🔹 Giả lập người dùng đang đăng nhập
            Long userId = 1L; // có thể thay sau bằng user thật từ session/login

            // 🔹 Gửi body tới ReservationService (8081)
            Map<String, Object> newReservation = Map.of(
                    "vehicleId", vehicleId,
                    "userId", userId,
                    "startDate", startDate,
                    "endDate", endDate,
                    "note", note != null ? note : ""
            );

            reservationService.createReservation(newReservation);

            // ✅ Sau khi đặt xong, quay lại trang của xe đó
            return "redirect:/reservations/book/" + vehicleId;

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
