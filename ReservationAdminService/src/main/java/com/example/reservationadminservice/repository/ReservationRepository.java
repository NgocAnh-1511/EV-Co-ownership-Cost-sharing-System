package com.example.reservationadminservice.repository;

import com.example.reservationadminservice.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {}
