package com.example.reservationadminservice.service;

import com.example.reservationadminservice.model.ReservationAdmin;
import com.example.reservationadminservice.model.VehicleAdmin;
import com.example.reservationadminservice.model.booking.BookingReservation;
import com.example.reservationadminservice.model.booking.BookingVehicle;
import com.example.reservationadminservice.repository.admin.AdminReservationRepository;
import com.example.reservationadminservice.repository.admin.AdminVehicleRepository;
import com.example.reservationadminservice.repository.booking.BookingReservationRepository;
import com.example.reservationadminservice.repository.booking.BookingVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service đồng bộ dữ liệu từ booking database sang admin database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataSyncService {

    private final BookingReservationRepository bookingReservationRepository;
    private final BookingVehicleRepository bookingVehicleRepository;
    private final AdminReservationRepository adminReservationRepository;
    private final AdminVehicleRepository adminVehicleRepository;

    /**
     * Đồng bộ tất cả dữ liệu từ booking DB sang admin DB
     * Chạy mỗi 5 phút
     */
    @Scheduled(cron = "${sync.schedule.cron}")
    public void syncAllData() {
        log.info("🔄 Bắt đầu đồng bộ dữ liệu từ booking DB sang admin DB...");
        
        try {
            syncVehicles();
            syncReservations();
            log.info("✅ Đồng bộ dữ liệu thành công lúc: {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("❌ Lỗi khi đồng bộ dữ liệu: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Đồng bộ vehicles
     */
    public void syncVehicles() {
        log.info("🚗 Đồng bộ vehicles...");
        
        try {
            // Đọc từ booking DB
            List<BookingVehicle> bookingVehicles = bookingVehicleRepository.findAll();
            log.info("📖 Đọc được {} vehicles từ booking DB", bookingVehicles.size());
            
            // Convert sang admin entities
            List<VehicleAdmin> adminVehicles = bookingVehicles.stream()
                    .map(this::convertToAdminVehicle)
                    .collect(Collectors.toList());
            
            // Xóa dữ liệu cũ trong admin DB
            adminVehicleRepository.deleteAll();
            log.info("🗑️ Đã xóa vehicles cũ trong admin DB");
            
            // Lưu vào admin DB
            adminVehicleRepository.saveAll(adminVehicles);
            
            log.info("✅ Đã đồng bộ {} vehicles", adminVehicles.size());
        } catch (Exception e) {
            log.error("❌ Lỗi khi đồng bộ vehicles: {}", e.getMessage(), e);
        }
    }

    /**
     * Đồng bộ reservations
     */
    public void syncReservations() {
        log.info("📅 Đồng bộ reservations...");
        
        try {
            // Đọc từ booking DB
            List<BookingReservation> bookingReservations = bookingReservationRepository.findAll();
            log.info("📖 Đọc được {} reservations từ booking DB", bookingReservations.size());
            
            // Convert sang admin entities
            List<ReservationAdmin> adminReservations = bookingReservations.stream()
                    .map(this::convertToAdminReservation)
                    .collect(Collectors.toList());
            
            log.info("🔄 Đã convert {} reservations", adminReservations.size());
            
            // Xóa dữ liệu cũ trong admin DB
            adminReservationRepository.deleteAll();
            log.info("🗑️ Đã xóa reservations cũ trong admin DB");
            
            // Lưu vào admin DB
            adminReservationRepository.saveAll(adminReservations);
            
            log.info("✅ Đã đồng bộ {} reservations", adminReservations.size());
        } catch (Exception e) {
            log.error("❌ Lỗi khi đồng bộ reservations: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Đồng bộ thủ công (để test)
     */
    public void syncManually() {
        log.info("🔧 Đồng bộ thủ công được kích hoạt...");
        syncAllData();
    }
    
    /**
     * Convert BookingVehicle sang VehicleAdmin
     */
    private VehicleAdmin convertToAdminVehicle(BookingVehicle booking) {
        VehicleAdmin admin = new VehicleAdmin();
        admin.setId(booking.getVehicleId());
        admin.setVehicleName(booking.getVehicleName());
        admin.setVehicleType(booking.getVehicleType());
        admin.setLicensePlate(booking.getLicensePlate());
        admin.setGroupId(booking.getGroupId());
        admin.setStatus(booking.getStatus());
        return admin;
    }
    
    /**
     * Convert BookingReservation sang ReservationAdmin
     */
    private ReservationAdmin convertToAdminReservation(BookingReservation booking) {
        ReservationAdmin admin = new ReservationAdmin();
        admin.setId(booking.getReservationId());
        admin.setVehicleId(booking.getVehicleId());
        admin.setUserId(booking.getUserId());
        admin.setStartDatetime(booking.getStartDatetime());
        admin.setEndDatetime(booking.getEndDatetime());
        admin.setPurpose(booking.getPurpose());
        admin.setStatus(booking.getStatus() != null ? booking.getStatus() : "BOOKED");
        // Convert LocalDateTime to Timestamp
        if (booking.getCreatedAt() != null) {
            admin.setCreatedAt(Timestamp.valueOf(booking.getCreatedAt()));
        }
        return admin;
    }
}


