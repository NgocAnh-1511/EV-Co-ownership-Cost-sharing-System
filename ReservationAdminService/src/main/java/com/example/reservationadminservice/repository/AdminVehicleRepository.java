package com.example.reservationadminservice.repository;

import com.example.reservationadminservice.model.VehicleAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminVehicleRepository extends JpaRepository<VehicleAdmin, Long> {
}
