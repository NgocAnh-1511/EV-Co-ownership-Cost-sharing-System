package com.example.reservationservice.service;

import com.example.reservationservice.model.*;
import com.example.reservationservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor
public class BookingService {
    private final ReservationRepository reservationRepo;
    private final VehicleRepository vehicleRepo;
    private final UserRepository userRepo;

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
        return reservationRepo.save(r);
    }
}
