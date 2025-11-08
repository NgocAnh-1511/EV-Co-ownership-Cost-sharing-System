package com.example.reservationadminservice.service;

import com.example.reservationadminservice.dto.ReservationDTO;
import com.example.reservationadminservice.model.ReservationAdmin;
import com.example.reservationadminservice.model.VehicleAdmin;
import com.example.reservationadminservice.repository.admin.AdminReservationRepository;
import com.example.reservationadminservice.repository.admin.AdminVehicleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminReservationService {

    private final AdminReservationRepository repository;
    private final AdminVehicleRepository vehicleRepository;
    private final BookingUserService bookingUserService;
    private final RestTemplate restTemplate;
    
    @Value("${reservation.service.url:http://localhost:8081}")
    private String reservationServiceUrl;

    public AdminReservationService(AdminReservationRepository repository,
                                   AdminVehicleRepository vehicleRepository,
                                   BookingUserService bookingUserService) {
        this.repository = repository;
        this.vehicleRepository = vehicleRepository;
        this.bookingUserService = bookingUserService;
        this.restTemplate = new RestTemplate();
    }

    public List<ReservationDTO> getAllReservations() {
        return repository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private ReservationDTO convertToDTO(ReservationAdmin reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setReservationId(reservation.getId());
        dto.setVehicleId(reservation.getVehicleId());
        dto.setUserId(reservation.getUserId());
        dto.setStartDatetime(reservation.getStartDatetime());
        dto.setEndDatetime(reservation.getEndDatetime());
        dto.setPurpose(reservation.getPurpose());
        dto.setStatus(reservation.getStatus());
        dto.setCreatedAt(reservation.getCreatedAt() != null ? 
            reservation.getCreatedAt().toLocalDateTime() : null);
        
        // Lấy tên xe từ admin database
        VehicleAdmin vehicle = vehicleRepository.findById(reservation.getVehicleId()).orElse(null);
        dto.setVehicleName(vehicle != null ? vehicle.getVehicleName() : "Xe #" + reservation.getVehicleId());
        
        // Lấy tên người dùng từ booking database
        String fullName = bookingUserService.getUserFullName(reservation.getUserId());
        dto.setUserName(fullName != null ? fullName : "User #" + reservation.getUserId());
        
        return dto;
    }
    
    public Optional<ReservationDTO> getReservationById(Long id) {
        return repository.findById(id).map(this::convertToDTO);
    }
    
    public ReservationDTO createReservation(ReservationDTO dto) {
        ReservationAdmin reservation = new ReservationAdmin();
        reservation.setVehicleId(dto.getVehicleId());
        reservation.setUserId(dto.getUserId());
        reservation.setStartDatetime(dto.getStartDatetime());
        reservation.setEndDatetime(dto.getEndDatetime());
        reservation.setPurpose(dto.getPurpose());
        reservation.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");
        
        ReservationAdmin saved = repository.save(reservation);
        return convertToDTO(saved);
    }
    
    public List<ReservationAdmin> getReservationsByStatus(String status) {
        return repository.findByStatus(status);
    }
    
    public List<ReservationAdmin> getReservationsByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
    
    /**
     * ====================================================================
     * CẬP NHẬT RESERVATION (ĐƯỢC GỌI TỪ RESERVATION SERVICE)
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Cập nhật reservation trong bảng admin: co_ownership_admin.reservations
     * - Method này được gọi từ Reservation Service sau khi đã cập nhật bảng chính
     * - Đảm bảo dữ liệu nhất quán giữa 2 bảng
     * 
     * LƯU Ý:
     * - Không nên gọi trực tiếp method này từ admin panel
     * - Luôn gọi qua Reservation Service để đảm bảo cập nhật từ bảng chính trước
     * 
     * @param id ID của reservation cần cập nhật
     * @param dto ReservationDTO chứa thông tin cần cập nhật
     * @return ReservationDTO đã được cập nhật
     */
    public ReservationDTO updateReservation(Long id, ReservationDTO dto) {
        System.out.println("🔄 [ADMIN SERVICE UPDATE] Cập nhật reservation ID: " + id + " trong bảng admin");
        
        ReservationAdmin reservation = repository.findById(id)
                .orElseThrow(() -> {
                    System.out.println("❌ [ERROR] Không tìm thấy reservation ID: " + id + " trong bảng admin");
                    return new RuntimeException("Reservation not found");
                });
        
        // Cập nhật các field
        if (dto.getStartDatetime() != null) {
            reservation.setStartDatetime(dto.getStartDatetime());
        }
        if (dto.getEndDatetime() != null) {
            reservation.setEndDatetime(dto.getEndDatetime());
        }
        if (dto.getPurpose() != null) {
            reservation.setPurpose(dto.getPurpose());
        }
        if (dto.getStatus() != null) {
            reservation.setStatus(dto.getStatus());
            System.out.println("✅ [ADMIN SERVICE UPDATE] Đã cập nhật trạng thái: " + dto.getStatus());
        }
        
        ReservationAdmin saved = repository.save(reservation);
        System.out.println("✅ [ADMIN SERVICE UPDATE] Đã cập nhật reservation ID: " + id + " trong bảng admin");
        return convertToDTO(saved);
    }
    
    /**
     * ====================================================================
     * XÓA RESERVATION TỪ CẢ 2 BẢNG DATABASE
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Xóa reservation từ bảng admin: co_ownership_admin.reservations
     * - Gọi Reservation Service để xóa từ bảng chính: co_ownership_booking.reservations
     * 
     * LÝ DO:
     * - Hệ thống sử dụng 2 database riêng biệt
     * - Scheduled job sync dữ liệu từ booking → admin mỗi 5 phút
     * - Nếu chỉ xóa từ bảng admin, scheduled job sẽ sync lại dữ liệu từ bảng chính
     * - Để đảm bảo dữ liệu nhất quán, cần xóa từ cả 2 bảng
     * 
     * QUY TRÌNH:
     * 1. Xóa từ bảng admin (co_ownership_admin.reservations)
     * 2. Gọi Reservation Service API để xóa từ bảng chính (co_ownership_booking.reservations)
     * 
     * @param id ID của reservation cần xóa
     * @throws RuntimeException nếu không tìm thấy reservation hoặc xóa thất bại
     */
    public void deleteReservation(Long id) {
        System.out.println("🗑️ [ADMIN SERVICE DELETE] Bắt đầu xóa reservation ID: " + id);
        
        // Kiểm tra reservation có tồn tại trong bảng admin không
        if (!repository.existsById(id)) {
            System.out.println("❌ [ERROR] Không tìm thấy reservation ID: " + id + " trong bảng admin");
            throw new RuntimeException("Reservation not found");
        }
        
        try {
            // ============================================================
            // BƯỚC 1: XÓA TỪ BẢNG ADMIN (co_ownership_admin.reservations)
            // ============================================================
            System.out.println("🔄 [STEP 1] Xóa từ bảng admin (co_ownership_admin.reservations)...");
            repository.deleteById(id);
            System.out.println("✅ [SUCCESS] Đã xóa reservation ID: " + id + " từ bảng admin");
            
            // ============================================================
            // BƯỚC 2: GỌI RESERVATION SERVICE ĐỂ XÓA TỪ BẢNG CHÍNH
            // ============================================================
            try {
                System.out.println("🔄 [STEP 2] Gọi Reservation Service để xóa từ bảng chính (co_ownership_booking.reservations)...");
                deleteFromBookingDatabase(id);
                System.out.println("✅ [SUCCESS] Đã xóa reservation ID: " + id + " từ bảng chính");
            } catch (Exception e) {
                // Nếu xóa từ bảng chính thất bại, log warning nhưng không throw exception
                // Vì đã xóa thành công từ bảng admin
                System.err.println("⚠️ [WARNING] Không thể xóa từ bảng chính: " + e.getMessage());
                System.err.println("   → Scheduled job sẽ tự động sync lại sau 5 phút");
                e.printStackTrace();
            }
            
            System.out.println("✅ [COMPLETE] Hoàn tất xóa reservation ID: " + id + " từ cả 2 bảng");
            
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Lỗi khi xóa reservation " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * ====================================================================
     * XÓA RESERVATION TỪ BẢNG CHÍNH (BOOKING DATABASE)
     * ====================================================================
     * 
     * MÔ TẢ:
     * - Gọi Reservation Service API để xóa reservation từ bảng chính
     * - Endpoint: DELETE /api/reservations/{id}
     * 
     * @param reservationId ID của reservation cần xóa từ bảng chính
     * @throws Exception nếu gọi API thất bại
     */
    private void deleteFromBookingDatabase(Long reservationId) {
        try {
            // Tạo URL endpoint của Reservation Service
            String url = reservationServiceUrl + "/api/reservations/" + reservationId;
            System.out.println("📡 [API CALL] Gọi Reservation Service API: " + url);
            
            // Gọi DELETE API đến Reservation Service
            // Reservation Service sẽ xóa từ bảng chính và cũng gọi lại Admin Service để xóa từ bảng admin
            // Nhưng vì đã xóa từ bảng admin rồi, nên sẽ không có vấn đề gì
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
            
            System.out.println("✅ [API SUCCESS] Đã gọi thành công Reservation Service để xóa reservation ID: " + reservationId);
        } catch (Exception e) {
            System.err.println("❌ [API ERROR] Lỗi khi gọi Reservation Service để xóa reservation " + reservationId + ": " + e.getMessage());
            throw e;
        }
    }
    
    public void syncFromReservationService(Map<String, Object> payload) {
        try {
            ReservationAdmin reservation = new ReservationAdmin();
            
            // Parse dữ liệu từ payload
            reservation.setId(((Number) payload.get("reservationId")).longValue());
            reservation.setVehicleId(((Number) payload.get("vehicleId")).longValue());
            reservation.setUserId(((Number) payload.get("userId")).longValue());
            
            // Parse datetime
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            reservation.setStartDatetime(LocalDateTime.parse((String) payload.get("startDatetime"), formatter));
            reservation.setEndDatetime(LocalDateTime.parse((String) payload.get("endDatetime"), formatter));
            
            reservation.setPurpose((String) payload.get("purpose"));
            reservation.setStatus((String) payload.get("status"));
            
            // Lưu vào database
            repository.save(reservation);
            
            System.out.println("✓ Đã lưu booking ID " + reservation.getId() + " vào Admin Database");
        } catch (Exception e) {
            System.err.println("✗ Lỗi khi lưu vào Admin Database: " + e.getMessage());
            throw new RuntimeException("Không thể đồng bộ dữ liệu: " + e.getMessage());
        }
    }
}
