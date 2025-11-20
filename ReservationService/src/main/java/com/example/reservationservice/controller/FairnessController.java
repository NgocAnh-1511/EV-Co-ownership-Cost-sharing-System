package com.example.reservationservice.controller;

import com.example.reservationservice.dto.FairnessSuggestionRequest;
import com.example.reservationservice.dto.FairnessSuggestionResponse;
import com.example.reservationservice.dto.FairnessSummaryDTO;
import com.example.reservationservice.service.FairnessEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fairness")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080"}, allowCredentials = "true")
public class FairnessController {

    private final FairnessEngineService fairnessEngineService;

    @GetMapping("/vehicles/{vehicleId}")
    public FairnessSummaryDTO getFairnessSummary(@PathVariable Long vehicleId,
                                                 @RequestParam(required = false, defaultValue = "30") Integer rangeDays) {
        return fairnessEngineService.buildSummary(vehicleId, rangeDays);
    }

    @PostMapping("/vehicles/{vehicleId}/suggest")
    public FairnessSuggestionResponse suggest(@PathVariable Long vehicleId,
                                              @RequestBody FairnessSuggestionRequest request) {
        return fairnessEngineService.suggest(vehicleId, request);
    }
}

