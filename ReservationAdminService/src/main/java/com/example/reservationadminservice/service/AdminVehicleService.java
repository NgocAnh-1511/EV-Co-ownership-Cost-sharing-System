package com.example.reservationadminservice.service;

import com.example.reservationadminservice.model.VehicleAdmin;
import com.example.reservationadminservice.repository.AdminVehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminVehicleService {

    private final AdminVehicleRepository repository;

    public AdminVehicleService(AdminVehicleRepository repository) {
        this.repository = repository;
    }

    public List<VehicleAdmin> getAllVehicles() {
        return repository.findAll();
    }
}
