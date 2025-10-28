package com.example.reservationservice.controller;

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
@CrossOrigin(origins = {"http://localhost:8080"})
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
    public Reservation create(@RequestParam Long vehicleId,
                              @RequestParam Long userId,
                              @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                              @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                              @RequestParam(value = "note", required = false) String note) {
        return bookingService.create(vehicleId, userId, start, end, note);
    }

}
