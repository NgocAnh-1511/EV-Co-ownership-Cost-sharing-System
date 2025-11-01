package com.example.reservationadminservice.service;

import com.example.reservationadminservice.dto.ReservationDTO;
import com.example.reservationadminservice.model.ReservationAdmin;
import com.example.reservationadminservice.model.VehicleAdmin;
import com.example.reservationadminservice.repository.admin.AdminReservationRepository;
import com.example.reservationadminservice.repository.admin.AdminVehicleRepository;
import org.springframework.stereotype.Service;

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

    public AdminReservationService(AdminReservationRepository repository,
                                   AdminVehicleRepository vehicleRepository,
                                   BookingUserService bookingUserService) {
        this.repository = repository;
        this.vehicleRepository = vehicleRepository;
        this.bookingUserService = bookingUserService;
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
    
    public List<ReservationAdmin> getReservationsByStatus(String status) {
        return repository.findByStatus(status);
    }
    
    public List<ReservationAdmin> getReservationsByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
    
    public ReservationDTO updateReservation(Long id, ReservationDTO dto) {
        ReservationAdmin reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        reservation.setStartDatetime(dto.getStartDatetime());
        reservation.setEndDatetime(dto.getEndDatetime());
        reservation.setPurpose(dto.getPurpose());
        reservation.setStatus(dto.getStatus());
        
        ReservationAdmin saved = repository.save(reservation);
        return convertToDTO(saved);
    }
    
    public void deleteReservation(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Reservation not found");
        }
        repository.deleteById(id);
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
