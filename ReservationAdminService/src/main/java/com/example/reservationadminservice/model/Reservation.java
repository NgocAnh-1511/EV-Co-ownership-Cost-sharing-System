package com.example.reservationadminservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;
    private Long vehicleId;
    private Long userId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String purpose;
    private String status;
}
