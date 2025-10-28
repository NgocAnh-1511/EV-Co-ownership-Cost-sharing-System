package com.example.reservationadminservice.repository;

import com.example.reservationadminservice.model.ReservationAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminReservationRepository extends JpaRepository<ReservationAdmin, Long> {
}
