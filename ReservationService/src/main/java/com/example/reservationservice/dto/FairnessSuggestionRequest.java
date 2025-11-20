package com.example.reservationservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FairnessSuggestionRequest {
    private Long userId;
    private LocalDateTime desiredStart;
    private Double durationHours = 2.0;
}

