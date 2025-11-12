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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
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
    
    @Value("${admin.service.url:http://localhost:8082}")
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
     * Update reservation
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

        return reservationRepo.save(reservation);
    }

    /**
     * ====================================================================
     * CẬP NHẬT TRẠNG THÁI RESERVATION TỪ CẢ 2 BẢNG DATABASE
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Cập nhật trạng thái reservation trong bảng chính: co_ownership_booking.reservations
     * - Cập nhật trạng thái reservation trong bảng admin: co_ownership_admin.reservations
     * 
     * LÝ DO:
     * - Hệ thống sử dụng 2 database riêng biệt
     * - Scheduled job sync dữ liệu từ booking → admin mỗi 5 phút
     * - Để đảm bảo UI hiển thị đúng trạng thái ngay lập tức, cần cập nhật cả 2 bảng
     * 
     * QUY TRÌNH:
     * 1. Cập nhật trạng thái trong bảng chính (co_ownership_booking.reservations)
     * 2. Flush và clear cache để đảm bảo thay đổi được commit
     * 3. Cập nhật trạng thái trong bảng admin (co_ownership_admin.reservations) qua Admin Service API
     * 
     * @param id ID của reservation cần cập nhật
     * @param status Trạng thái mới (BOOKED, COMPLETED, CANCELLED)
     * @return Reservation đã được cập nhật
     * @throws RuntimeException nếu không tìm thấy reservation hoặc cập nhật thất bại
     */
    /**
     * ====================================================================
     * CẬP NHẬT TRẠNG THÁI RESERVATION - TÁCH THÀNH 2 TRANSACTION RIÊNG
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Cập nhật trạng thái trong bảng chính TRƯỚC (transaction riêng, REQUIRES_NEW)
     * - Sau đó cập nhật trạng thái trong bảng admin (không ảnh hưởng transaction chính)
     * 
     * LÝ DO TÁCH TRANSACTION:
     * - REQUIRES_NEW tạo transaction mới, commit ngay lập tức
     * - Đảm bảo bảng chính luôn được cập nhật thành công
     * - Nếu gọi Admin Service thất bại, không rollback transaction chính
     * 
     * @param id ID của reservation cần cập nhật
     * @param status Trạng thái mới (BOOKED, COMPLETED, CANCELLED)
     * @return Reservation đã được cập nhật
     */
    @PutMapping("/reservations/{id}/status")
    public Reservation updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        System.out.println("🔄 [UPDATE STATUS] Bắt đầu cập nhật trạng thái reservation ID: " + id + " → " + status);
        
        try {
            // ============================================================
            // BƯỚC 1: CẬP NHẬT TRẠNG THÁI TRONG BẢNG CHÍNH (TRƯỚC) - TRANSACTION RIÊNG
            // ============================================================
            Reservation updatedReservation = updateStatusInBookingDatabase(id, status);
            
            // ============================================================
            // BƯỚC 2: CẬP NHẬT TRẠNG THÁI TRONG BẢNG ADMIN (SAU) - KHÔNG ẢNH HƯỞNG TRANSACTION CHÍNH
            // ============================================================
            try {
                System.out.println("🔄 [STEP 2] Cập nhật trạng thái trong bảng admin (co_ownership_admin.reservations)...");
                updateStatusInAdminDatabase(id, status);
                System.out.println("✅ [SUCCESS] Đã cập nhật trạng thái reservation ID: " + id + " → " + status + " trong bảng admin");
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // Nếu lỗi 404 (Not Found), có nghĩa là reservation không tồn tại trong bảng admin
                // (có thể do chưa được sync)
                if (e.getStatusCode().value() == 404) {
                    System.out.println("ℹ️ [INFO] Reservation không tồn tại trong bảng admin (sẽ được sync trong lần sync tiếp theo)");
                } else {
                    System.err.println("⚠️ [WARNING] Không thể cập nhật trạng thái trong bảng admin: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // Nếu cập nhật từ bảng admin thất bại, chỉ log warning
                // Không throw exception vì đã cập nhật thành công từ bảng chính
                // Scheduled job sẽ tự động sync lại sau 5 phút
                System.err.println("⚠️ [WARNING] Không thể cập nhật trạng thái trong bảng admin: " + e.getMessage());
                System.err.println("   → Scheduled job sẽ tự động sync lại sau 5 phút");
                e.printStackTrace();
            }
            
            System.out.println("✅ [COMPLETE] Hoàn tất cập nhật trạng thái reservation ID: " + id + " → " + status + " từ cả 2 bảng");
            
            return updatedReservation;
            
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Lỗi khi cập nhật trạng thái reservation " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating reservation status: " + e.getMessage(), e);
        }
    }
    
    /**
     * ====================================================================
     * CẬP NHẬT TRẠNG THÁI TRONG BẢNG CHÍNH (TRANSACTION RIÊNG - REQUIRES_NEW)
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Cập nhật trạng thái trong bảng chính (co_ownership_booking.reservations)
     * - Sử dụng REQUIRES_NEW để tạo transaction mới, commit ngay lập tức
     * - Flush, refresh và verify để đảm bảo status đã được cập nhật
     * 
     * @param reservationId ID của reservation cần cập nhật
     * @param status Trạng thái mới
     * @return Reservation đã được cập nhật
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    private Reservation updateStatusInBookingDatabase(Long reservationId, String status) {
        System.out.println("🔄 [STEP 1] Cập nhật trạng thái trong bảng chính (co_ownership_booking.reservations)...");
        System.out.println("   → Đây là bảng CHÍNH, UI người dùng sẽ query từ đây");
        System.out.println("   → Sử dụng REQUIRES_NEW để commit ngay lập tức");
        
        // Tìm reservation trong database chính
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> {
                    System.out.println("❌ [ERROR] Không tìm thấy reservation ID: " + reservationId);
                    return new RuntimeException("Reservation not found");
                });
        
        // Lưu trạng thái cũ để log
        String oldStatus = reservation.getStatus() != null ? reservation.getStatus().toString() : "null";
        System.out.println("   → Trạng thái cũ: " + oldStatus + " → Trạng thái mới: " + status);
        
        // Cập nhật trạng thái
        reservation.setStatus(Reservation.Status.valueOf(status));
        Reservation updatedReservation = reservationRepo.save(reservation);
        System.out.println("   → Đã save reservation với trạng thái mới: " + updatedReservation.getStatus());
        
        // Force flush để đảm bảo thay đổi được ghi vào database
        entityManager.flush();
        System.out.println("   → Đã flush thay đổi vào database");
        
        // QUAN TRỌNG: Refresh entity từ database để đảm bảo lấy dữ liệu mới nhất
        entityManager.refresh(updatedReservation);
        System.out.println("   → Đã refresh entity từ database");
        
        // Verify sau khi refresh - QUAN TRỌNG: Đảm bảo status đã được cập nhật
        Reservation verifyReservation = reservationRepo.findById(reservationId).orElse(null);
        if (verifyReservation != null) {
            System.out.println("   → Verified: Status trong database = " + verifyReservation.getStatus());
            if (!verifyReservation.getStatus().toString().equals(status)) {
                System.err.println("   ⚠️ ERROR: Status không khớp! Expected: " + status + ", Actual: " + verifyReservation.getStatus());
                throw new RuntimeException("Status verification failed: Expected " + status + " but got " + verifyReservation.getStatus());
            }
            System.out.println("   → ✅ Verified OK: Status đã được cập nhật thành công trong database");
        } else {
            System.err.println("   ⚠️ ERROR: Không tìm thấy reservation sau khi save!");
            throw new RuntimeException("Reservation not found after save");
        }
        
        // Clear cache để đảm bảo query sau này (từ UI người dùng) lấy dữ liệu mới nhất
        entityManager.clear();
        System.out.println("   → Đã clear cache, query tiếp theo sẽ lấy dữ liệu mới từ database");
        
        // QUAN TRỌNG: Verify lại sau khi clear cache
        Reservation finalVerify = reservationRepo.findById(reservationId).orElse(null);
        if (finalVerify != null) {
            System.out.println("   → Final Verify: Status sau khi clear cache = " + finalVerify.getStatus());
            if (!finalVerify.getStatus().toString().equals(status)) {
                System.err.println("   ⚠️ ERROR: Status không khớp sau khi clear cache! Expected: " + status + ", Actual: " + finalVerify.getStatus());
                throw new RuntimeException("Status verification failed after cache clear: Expected " + status + " but got " + finalVerify.getStatus());
            }
            System.out.println("   → ✅ Final Verify OK: Status đã được cập nhật thành công");
        }
        
        System.out.println("✅ [SUCCESS] Đã cập nhật trạng thái reservation ID: " + reservationId + " → " + status + " trong bảng chính");
        System.out.println("   → Transaction sẽ commit ngay lập tức (REQUIRES_NEW)");
        System.out.println("   → UI người dùng sẽ thấy trạng thái mới ngay lập tức khi refresh");
        
        return updatedReservation;
    }
    
    /**
     * ====================================================================
     * CẬP NHẬT TRẠNG THÁI RESERVATION TRONG BẢNG ADMIN DATABASE
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Gọi Admin Service API để cập nhật trạng thái reservation trong bảng admin
     * - Endpoint: PUT /api/admin/reservations/{id}
     * 
     * @param reservationId ID của reservation cần cập nhật
     * @param status Trạng thái mới
     * @throws Exception nếu gọi API thất bại
     */
    private void updateStatusInAdminDatabase(Long reservationId, String status) {
        try {
            // Lấy thông tin reservation hiện tại để gửi đầy đủ thông tin
            Reservation reservation = reservationRepo.findById(reservationId).orElse(null);
            if (reservation == null) {
                System.err.println("⚠️ [WARNING] Không tìm thấy reservation ID: " + reservationId + " để lấy thông tin");
                return;
            }
            
            // Tạo URL endpoint của Admin Service
            String url = adminServiceUrl + "/api/admin/reservations/" + reservationId;
            System.out.println("📡 [API CALL] Gọi Admin Service API để cập nhật trạng thái: " + url);
            
            // Tạo request body với đầy đủ thông tin (Admin Service cần ReservationDTO)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("status", status);
            // Giữ nguyên các field khác từ reservation hiện tại
            if (reservation.getStartDatetime() != null) {
                requestBody.put("startDatetime", reservation.getStartDatetime().toString());
            }
            if (reservation.getEndDatetime() != null) {
                requestBody.put("endDatetime", reservation.getEndDatetime().toString());
            }
            if (reservation.getPurpose() != null) {
                requestBody.put("purpose", reservation.getPurpose());
            }
            if (reservation.getVehicle() != null) {
                requestBody.put("vehicleId", reservation.getVehicle().getVehicleId());
            }
            if (reservation.getUser() != null) {
                requestBody.put("userId", reservation.getUser().getUserId());
            }
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> request = new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            // Gọi PUT API đến Admin Service
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            
            System.out.println("✅ [API SUCCESS] Đã gọi thành công Admin Service để cập nhật trạng thái reservation ID: " + reservationId);
        } catch (Exception e) {
            System.err.println("❌ [API ERROR] Lỗi khi gọi Admin Service để cập nhật trạng thái reservation " + reservationId + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * ====================================================================
     * XÓA RESERVATION TỪ CẢ 2 BẢNG DATABASE
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Xóa reservation từ bảng chính: co_ownership_booking.reservations
     * - Xóa reservation từ bảng admin: co_ownership_admin.reservations
     * 
     * LÝ DO:
     * - Hệ thống sử dụng 2 database riêng biệt:
     *   + co_ownership_booking: Database chính cho Reservation Service
     *   + co_ownership_admin: Database cho Admin Service (được sync tự động)
     * - Scheduled job sync dữ liệu từ booking → admin mỗi 5 phút
     * - Để đảm bảo dữ liệu nhất quán, cần xóa từ cả 2 bảng ngay lập tức
     * 
     * QUY TRÌNH:
     * 1. Xóa từ bảng chính (co_ownership_booking.reservations)
     * 2. Flush và clear cache để đảm bảo thay đổi được commit
     * 3. Xác minh đã xóa thành công từ bảng chính
     * 4. Xóa từ bảng admin (co_ownership_admin.reservations) qua Admin Service API
     * 
     * @param id ID của reservation cần xóa
     * @throws RuntimeException nếu không tìm thấy reservation hoặc xóa thất bại
     */
    @DeleteMapping("/reservations/{id}")
    @Transactional
    public void deleteReservation(@PathVariable Long id) {
        System.out.println("🗑️ [DELETE] Bắt đầu xóa reservation ID: " + id);
        
        // Bước 1: Tìm reservation trong database chính
        Reservation reservation = reservationRepo.findById(id)
            .orElseThrow(() -> {
                System.out.println("❌ [ERROR] Không tìm thấy reservation ID: " + id);
                return new RuntimeException("Reservation not found: " + id);
            });
        
        // Log thông tin reservation trước khi xóa
        System.out.println("📋 [INFO] Thông tin reservation cần xóa:");
        System.out.println("   - Vehicle ID: " + (reservation.getVehicle() != null ? reservation.getVehicle().getVehicleId() : "null"));
        System.out.println("   - User ID: " + (reservation.getUser() != null ? reservation.getUser().getUserId() : "null"));
        System.out.println("   - Status: " + reservation.getStatus());
        
        try {
            // ============================================================
            // BƯỚC 2: XÓA TỪ BẢNG CHÍNH (co_ownership_booking.reservations)
            // ============================================================
            System.out.println("🔄 [STEP 1] Xóa từ bảng chính (co_ownership_booking.reservations)...");
            reservationRepo.delete(reservation);
            
            // Force flush để đảm bảo thay đổi được commit ngay lập tức
            entityManager.flush();
            // Clear cache để đảm bảo query sau này lấy dữ liệu mới nhất
            entityManager.clear();
            
            System.out.println("✅ [SUCCESS] Đã xóa reservation ID: " + id + " từ bảng chính");
            System.out.println("✅ [SUCCESS] Đã flush và clear EntityManager cache");
            
            // ============================================================
            // BƯỚC 3: XÁC MINH ĐÃ XÓA THÀNH CÔNG TỪ BẢNG CHÍNH
            // ============================================================
            boolean stillExists = reservationRepo.existsById(id);
            if (stillExists) {
                System.out.println("⚠️ [WARNING] Reservation vẫn còn tồn tại sau khi xóa!");
                throw new RuntimeException("Failed to delete reservation: still exists after delete operation");
            } else {
                System.out.println("✅ [VERIFIED] Đã xác minh: Reservation đã được xóa khỏi bảng chính");
            }
            
            // ============================================================
            // BƯỚC 4: XÓA TỪ BẢNG ADMIN (co_ownership_admin.reservations)
            // ============================================================
            try {
                System.out.println("🔄 [STEP 2] Xóa từ bảng admin (co_ownership_admin.reservations)...");
                deleteFromAdminDatabase(id);
                System.out.println("✅ [SUCCESS] Đã xóa reservation ID: " + id + " từ bảng admin");
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // Nếu lỗi 404 (Not Found), có nghĩa là reservation đã được xóa từ bảng admin rồi
                // (có thể do Admin Service đã xóa trước đó)
                if (e.getStatusCode().value() == 404) {
                    System.out.println("ℹ️ [INFO] Reservation không tồn tại trong bảng admin (có thể đã được xóa trước đó)");
                } else {
                    System.err.println("⚠️ [WARNING] Không thể xóa từ bảng admin: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // Nếu xóa từ bảng admin thất bại, chỉ log warning
                // Không throw exception vì đã xóa thành công từ bảng chính
                // Scheduled job sẽ tự động sync lại sau 5 phút
                System.err.println("⚠️ [WARNING] Không thể xóa từ bảng admin: " + e.getMessage());
                System.err.println("   → Scheduled job sẽ tự động sync lại sau 5 phút");
                e.printStackTrace();
            }
            
            System.out.println("✅ [COMPLETE] Hoàn tất xóa reservation ID: " + id + " từ cả 2 bảng");
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.err.println("❌ [ERROR] DataIntegrityViolationException khi xóa reservation " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot delete reservation: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Lỗi khi xóa reservation " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting reservation: " + e.getMessage(), e);
        }
    }
    
    /**
     * ====================================================================
     * XÓA RESERVATION TỪ BẢNG ADMIN DATABASE
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Gọi Admin Service API để xóa reservation từ bảng admin
     * - Endpoint: DELETE /api/admin/reservations/{id}
     * 
     * @param reservationId ID của reservation cần xóa từ bảng admin
     * @throws Exception nếu gọi API thất bại
     */
    private void deleteFromAdminDatabase(Long reservationId) {
        try {
            // Tạo URL endpoint của Admin Service
            String url = adminServiceUrl + "/api/admin/reservations/" + reservationId;
            System.out.println("📡 [API CALL] Gọi Admin Service API: " + url);
            
            // Gọi DELETE API đến Admin Service
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
            
            System.out.println("✅ [API SUCCESS] Đã gọi thành công Admin Service để xóa reservation ID: " + reservationId);
        } catch (Exception e) {
            System.err.println("❌ [API ERROR] Lỗi khi gọi Admin Service để xóa reservation " + reservationId + ": " + e.getMessage());
            throw e;
        }
    }

}
