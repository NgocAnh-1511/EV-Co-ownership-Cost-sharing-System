package com.example.reservationservice.controller;

import com.example.reservationservice.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8080"}, allowCredentials = "true")
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping("/vehicles")
    public List<Map<String,Object>> listVehicles() {
        return vehicleService.getAllVehiclesWithOwners();
    }
    
    // Get vehicles for logged-in user
    @GetMapping("/my-vehicles")
    public List<Map<String,Object>> getMyVehicles(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return List.of(); // Return empty list if not logged in
        }
        return vehicleService.getUserVehicles(userId);
    }
}
