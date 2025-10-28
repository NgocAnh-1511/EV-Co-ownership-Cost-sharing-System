package com.example.reservationadminservice.service;

import com.example.reservationadminservice.model.ReservationAdmin;
import com.example.reservationadminservice.repository.AdminReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminReservationService {

    private final AdminReservationRepository repository;

    public AdminReservationService(AdminReservationRepository repository) {
        this.repository = repository;
    }

    public List<ReservationAdmin> getAllReservations() {
        return repository.findAll();
    }
}
