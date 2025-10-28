package com.example.reservationservice.controller;

import com.example.reservationservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8080"})
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping("/vehicles")
    public List<Map<String,Object>> listVehicles() {
        return vehicleService.getAllVehiclesWithOwners();
    }
}
