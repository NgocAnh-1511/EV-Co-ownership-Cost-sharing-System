package com.example.reservationservice.service;

import com.example.reservationservice.model.*;
import com.example.reservationservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service @RequiredArgsConstructor
public class BookingService {
    private final ReservationRepository reservationRepo;
    private final VehicleRepository vehicleRepo;
    private final UserRepository userRepo;
    private final RestTemplate restTemplate;
    
    @Value("${admin.service.url:http://localhost:8082}")
    private String adminServiceUrl;

    public boolean isAvailable(Long vehicleId, LocalDateTime start, LocalDateTime end) {
        long overlaps = reservationRepo.countOverlap(vehicleId, start, end);
        return overlaps == 0;
    }

    @Transactional
    public Reservation create(Long vehicleId, Long userId,
                              LocalDateTime start, LocalDateTime end, String purpose) {
        if (!isAvailable(vehicleId, start, end)) {
            throw new IllegalStateException("Time range overlaps existing reservation");
        }
        Vehicle v = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Reservation r = new Reservation();
        r.setVehicle(v);
        r.setUser(u);
        r.setStartDatetime(start);
        r.setEndDatetime(end);
        r.setPurpose(purpose);
        r.setStatus(Reservation.Status.BOOKED);
        Reservation savedReservation = reservationRepo.save(r);
        
        // Đồng bộ dữ liệu sang Admin Service
        syncToAdminService(savedReservation);
        
        return savedReservation;
    }
    
    private void syncToAdminService(Reservation reservation) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("reservationId", reservation.getReservationId());
            payload.put("vehicleId", reservation.getVehicle().getVehicleId());
            payload.put("vehicleName", reservation.getVehicle().getVehicleName());
            payload.put("userId", reservation.getUser().getUserId());
            payload.put("userName", reservation.getUser().getFullName());
            payload.put("startDatetime", reservation.getStartDatetime().toString());
            payload.put("endDatetime", reservation.getEndDatetime().toString());
            payload.put("purpose", reservation.getPurpose());
            payload.put("status", reservation.getStatus().toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            String url = adminServiceUrl + "/api/admin/reservations/sync";
            restTemplate.postForObject(url, request, String.class);
            
            System.out.println("✓ Đã đồng bộ booking ID " + reservation.getReservationId() + " sang Admin Service");
        } catch (Exception e) {
            System.err.println("✗ Lỗi khi đồng bộ sang Admin Service: " + e.getMessage());
            // Không throw exception để không ảnh hưởng đến việc tạo booking
        }
    }
}
