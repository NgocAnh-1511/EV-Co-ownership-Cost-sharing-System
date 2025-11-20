package com.example.reservationservice.controller;

import com.example.reservationservice.dto.ReservationRequest;
import com.example.reservationservice.model.Reservation;
import com.example.reservationservice.repository.ReservationRepository;
import com.example.reservationservice.service.BookingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8080"}, allowCredentials = "true")
public class ReservationController {

    private final BookingService bookingService;
    private final ReservationRepository reservationRepo;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Value("${admin.service.url:${API_GATEWAY_URL:http://localhost:8084}}")
    private String adminServiceUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * ====================================================================
     * LẤY DANH SÁCH RESERVATIONS THEO VEHICLE ID
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Lấy danh sách reservations từ bảng chính (co_ownership_booking.reservations)
     * - Clear cache trước khi query để đảm bảo lấy dữ liệu mới nhất
     * - Dữ liệu này được hiển thị trên UI người dùng
     * 
     * LƯU Ý:
     * - Luôn clear cache trước khi query để đảm bảo dữ liệu mới nhất
     * - Query từ bảng chính, không phải bảng admin
     * 
     * @param vehicleId ID của vehicle cần lấy danh sách reservations
     * @return Danh sách reservations của vehicle
     */
    @GetMapping("/vehicles/{vehicleId}/reservations")
    public List<Reservation> vehicleCalendar(@PathVariable Long vehicleId) {
        System.out.println("📋 [FETCH RESERVATIONS] Lấy danh sách reservations cho vehicle: " + vehicleId);
        
        // Clear EntityManager cache trước khi query để đảm bảo lấy dữ liệu mới nhất
        // Điều này đảm bảo khi admin cập nhật status, UI người dùng sẽ thấy ngay
        entityManager.clear();
        System.out.println("🧹 [CACHE CLEARED] Đã clear EntityManager cache");
        
        List<Reservation> reservations = reservationRepo.findByVehicle_VehicleIdOrderByStartDatetimeAsc(vehicleId);
        System.out.println("✅ [FETCH SUCCESS] Tìm thấy " + (reservations != null ? reservations.size() : 0) + " reservations cho vehicle " + vehicleId);
        
        if (reservations != null && !reservations.isEmpty()) {
            reservations.forEach(r -> System.out.println("   - ID: " + r.getReservationId() + ", Status: " + r.getStatus() + ", Start: " + r.getStartDatetime()));
        }
        
        return reservations;
    }

    @GetMapping("/availability")
    public boolean isAvailable(@RequestParam Long vehicleId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return bookingService.isAvailable(vehicleId, start, end);
    }

    @PostMapping("/reservations")
    public Reservation create(@RequestBody ReservationRequest request) {
        return bookingService.create(
                request.getVehicleId(),
                request.getUserId(),
                request.getStartDatetime(),
                request.getEndDatetime(),
                request.getPurpose()
        );
    }

    /**
     * Get all reservations (for admin)
     */
    @GetMapping("/reservations")
    public List<Reservation> getAllReservations() {
        return reservationRepo.findAll();
    }

    /**
     * Get reservation by ID
     */
    @GetMapping("/reservations/{id}")
    public Reservation getReservation(@PathVariable Long id) {
        return reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
    }

    /**
     * Cập nhật reservation
     */
    @PutMapping("/reservations/{id}")
    public Reservation updateReservation(
            @PathVariable Long id,
            @RequestBody ReservationRequest request) {
        Reservation reservation = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        reservation.setStartDatetime(request.getStartDatetime());
        reservation.setEndDatetime(request.getEndDatetime());
        reservation.setPurpose(request.getPurpose());
        if (request.getStatus() != null) {
            reservation.setStatus(Reservation.Status.valueOf(request.getStatus()));
        }

        Reservation updated = reservationRepo.save(reservation);
        
        // Đồng bộ sang admin database
        syncToAdmin(id, updated);
        
        return updated;
    }
    
    /**
     * Đồng bộ reservation sang admin database
     */
    private void syncToAdmin(Long id, Reservation reservation) {
        try {
            String url = adminServiceUrl + "/api/admin/reservations/" + id;
            Map<String, Object> body = new HashMap<>();
            body.put("reservationId", id);
            body.put("status", reservation.getStatus() != null ? reservation.getStatus().toString() : "BOOKED");
            body.put("startDatetime", reservation.getStartDatetime() != null ? reservation.getStartDatetime().toString() : null);
            body.put("endDatetime", reservation.getEndDatetime() != null ? reservation.getEndDatetime().toString() : null);
            body.put("purpose", reservation.getPurpose());
            body.put("vehicleId", reservation.getVehicle() != null ? reservation.getVehicle().getVehicleId() : null);
            body.put("userId", reservation.getUser() != null ? reservation.getUser().getUserId() : null);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> request = new org.springframework.http.HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);
        } catch (Exception e) {
            System.err.println("⚠️ Không thể đồng bộ sang admin: " + e.getMessage());
        }
    }


    /**
     * Xóa reservation
     */
    @DeleteMapping("/reservations/{id}")
    public void deleteReservation(@PathVariable Long id) {
        Reservation reservation = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        reservationRepo.delete(reservation);
        
        // Xóa từ admin database
        try {
            String url = adminServiceUrl + "/api/admin/reservations/" + id;
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        } catch (Exception e) {
            System.err.println("⚠️ Không thể xóa từ admin: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật trạng thái reservation
     */
    @PutMapping("/reservations/{id}/status")
    public Reservation updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Reservation reservation = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        reservation.setStatus(Reservation.Status.valueOf(status.toUpperCase()));
        Reservation updated = reservationRepo.save(reservation);
        
        // Đồng bộ sang admin database
        syncToAdmin(id, updated);
        
        return updated;
    }

}
