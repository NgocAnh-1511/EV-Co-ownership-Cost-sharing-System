package com.example.reservationservice.repository;

import com.example.reservationservice.model.Reservation;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.vehicle.vehicleId = :vehicleId
          AND r.status = 'BOOKED'
          AND r.startDatetime < :end
          AND r.endDatetime > :start
    """)
    long countOverlap(@Param("vehicleId") Long vehicleId,
                      @Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end);

    @Query("""
        SELECT r FROM Reservation r
        LEFT JOIN FETCH r.vehicle
        LEFT JOIN FETCH r.user
        WHERE r.vehicle.vehicleId = :vehicleId
        ORDER BY r.startDatetime DESC
    """)
    List<Reservation> findByVehicle_VehicleIdOrderByStartDatetimeAsc(@Param("vehicleId") Long vehicleId);
}
