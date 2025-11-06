package com.example.reservationservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationRequest {
    private Long userId;
    private Long vehicleId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String purpose;
    private String status;
}

