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

    List<Reservation> findByVehicle_VehicleIdOrderByStartDatetimeAsc(Long vehicleId);
}
