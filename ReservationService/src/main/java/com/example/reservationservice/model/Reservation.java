package com.example.reservationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "reservations")
@Data @NoArgsConstructor @AllArgsConstructor
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "vehicleGroup"})
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Column(name = "start_datetime")
    private LocalDateTime startDatetime;
    
    @Column(name = "end_datetime")
    private LocalDateTime endDatetime;
    
    private String purpose;

    @Enumerated(EnumType.STRING)
    private Status status = Status.BOOKED;

    public enum Status { BOOKED, COMPLETED, CANCELLED }
}
