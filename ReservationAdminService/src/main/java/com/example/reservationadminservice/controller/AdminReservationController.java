package com.example.reservationadminservice.controller;

import com.example.reservationadminservice.model.ReservationAdmin;
import com.example.reservationadminservice.service.AdminReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@CrossOrigin(origins = "http://localhost:8080")
public class AdminReservationController {

    private final AdminReservationService service;

    public AdminReservationController(AdminReservationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReservationAdmin> getAllReservations() {
        return service.getAllReservations();
    }
}
