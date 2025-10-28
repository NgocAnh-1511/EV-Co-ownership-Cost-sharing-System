package com.example.reservationadminservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class ReservationAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private java.time.LocalDateTime startDatetime;

    @Column(nullable = false)
    private java.time.LocalDateTime endDatetime;

    private String purpose;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false, insertable = false)
    private java.sql.Timestamp createdAt;
}
