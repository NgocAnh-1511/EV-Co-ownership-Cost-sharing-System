package com.example.reservationservice.controller;

import com.example.reservationservice.dto.ReservationRequest;
import com.example.reservationservice.model.Reservation;
import com.example.reservationservice.repository.ReservationRepository;
import com.example.reservationservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8080"}, allowCredentials = "true")
public class ReservationController {

    private final BookingService bookingService;
    private final ReservationRepository reservationRepo;

    @GetMapping("/vehicles/{vehicleId}/reservations")
    public List<Reservation> vehicleCalendar(@PathVariable Long vehicleId) {
        return reservationRepo.findByVehicle_VehicleIdOrderByStartDatetimeAsc(vehicleId);
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
     * Update reservation status only
     */
    @PutMapping("/reservations/{id}/status")
    public Reservation updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Reservation reservation = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        reservation.setStatus(Reservation.Status.valueOf(status));
        return reservationRepo.save(reservation);
    }

    /**
     * Delete reservation
     */
    @DeleteMapping("/reservations/{id}")
    public void deleteReservation(@PathVariable Long id) {
        if (!reservationRepo.existsById(id)) {
            throw new RuntimeException("Reservation not found");
        }
        reservationRepo.deleteById(id);
    }

}
