package com.example.aiservice.controller;

import com.example.aiservice.dto.*;
import com.example.aiservice.model.OwnershipInfo;
import com.example.aiservice.service.AIAnalysisService;
import com.example.aiservice.service.OwnershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho AI recommendation và analysis
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationController {
    
    private final AIAnalysisService analysisService;
    private final OwnershipService ownershipService;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${reservation.service.url:http://localhost:8081}")
    private String reservationServiceUrl;
    
    /**
     * Phân tích sử dụng xe và tạo gợi ý
     * POST /api/ai/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeVehicleUsage(@RequestBody AnalysisRequest request) {
        try {
            log.info("Received analysis request for vehicle {} in group {}", 
                request.getVehicleId(), request.getGroupId());
            
            // Set default period if not provided (last 30 days)
            if (request.getPeriodStart() == null) {
                request.setPeriodStart(LocalDateTime.now().minusDays(30));
            }
            if (request.getPeriodEnd() == null) {
                request.setPeriodEnd(LocalDateTime.now());
            }
            
            AnalysisResponse response = analysisService.analyzeVehicleUsage(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error analyzing vehicle usage: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Analysis failed",
                    "message", e.getMessage()
                ));
        }
    }
    
    /**
     * Lấy tất cả gợi ý (cho admin)
     * GET /api/ai/recommendations/all
     */
    @GetMapping("/recommendations/all")
    public ResponseEntity<?> getAllRecommendations(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority) {
        try {
            log.info("Getting all recommendations - type: {}, priority: {}", type, priority);
            List<RecommendationDTO> recommendations = 
                analysisService.getAllRecommendations(type, priority);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error getting all recommendations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy gợi ý cho một user cụ thể
     * GET /api/ai/recommendations/user/{userId}
     */
    @GetMapping("/recommendations/user/{userId}")
    public ResponseEntity<?> getRecommendationsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "3") int limit) {
        try {
            log.info("Getting recommendations for user: {}, limit: {}", userId, limit);
            List<RecommendationDTO> recommendations = 
                analysisService.getRecommendationsByUser(userId, limit);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error getting user recommendations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy gợi ý cho một nhóm
     * GET /api/ai/recommendations?groupId=1&status=ACTIVE
     */
    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(
            @RequestParam Long groupId,
            @RequestParam(required = false) String status) {
        try {
            List<RecommendationDTO> recommendations = 
                analysisService.getRecommendations(groupId, status);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error getting recommendations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Đánh dấu recommendation đã đọc
     * PUT /api/ai/recommendations/{id}/read
     */
    @PutMapping("/recommendations/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            analysisService.markRecommendationAsRead(id);
            return ResponseEntity.ok(Map.of("message", "Marked as read"));
        } catch (Exception e) {
            log.error("Error marking recommendation as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Gợi ý thời gian đặt lịch phù hợp
     * POST /api/ai/suggest-schedule
     */
    @PostMapping("/suggest-schedule")
    public ResponseEntity<?> suggestSchedule(@RequestBody ScheduleSuggestionRequest request) {
        try {
            ScheduleSuggestionResponse response = analysisService.suggestSchedule(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error suggesting schedule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Thêm/Cập nhật thông tin sở hữu
     * POST /api/ai/ownership
     */
    @PostMapping("/ownership")
    public ResponseEntity<?> saveOwnership(@Valid @RequestBody OwnershipRequest request) {
        try {
            OwnershipInfo ownership = ownershipService.saveOwnership(request);
            return ResponseEntity.ok(ownership);
        } catch (Exception e) {
            log.error("Error saving ownership: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách owners của một vehicle
     * GET /api/ai/ownership/vehicle/{vehicleId}
     */
    @GetMapping("/ownership/vehicle/{vehicleId}")
    public ResponseEntity<?> getOwnersByVehicle(@PathVariable Long vehicleId) {
        try {
            List<OwnershipInfo> owners = ownershipService.getOwnersByVehicle(vehicleId);
            return ResponseEntity.ok(owners);
        } catch (Exception e) {
            log.error("Error getting owners: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách owners trong một nhóm
     * GET /api/ai/ownership/group/{groupId}
     */
    @GetMapping("/ownership/group/{groupId}")
    public ResponseEntity<?> getOwnersByGroup(@PathVariable Long groupId) {
        try {
            List<OwnershipInfo> owners = ownershipService.getOwnersByGroup(groupId);
            return ResponseEntity.ok(owners);
        } catch (Exception e) {
            log.error("Error getting owners: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Kiểm tra tổng ownership có hợp lệ không
     * GET /api/ai/ownership/validate?groupId=1&vehicleId=1
     */
    @GetMapping("/ownership/validate")
    public ResponseEntity<?> validateOwnership(
            @RequestParam Long groupId,
            @RequestParam Long vehicleId) {
        try {
            boolean valid = ownershipService.validateTotalOwnership(groupId, vehicleId);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", valid);
            if (!valid) {
                response.put("message", "Tổng tỷ lệ sở hữu phải bằng 100%");
            } else {
                response.put("message", "Tỷ lệ sở hữu hợp lệ");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating ownership: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Xóa thông tin sở hữu
     * DELETE /api/ai/ownership/{id}
     */
    @DeleteMapping("/ownership/{id}")
    public ResponseEntity<?> deleteOwnership(@PathVariable Long id) {
        try {
            ownershipService.deleteOwnership(id);
            return ResponseEntity.ok(Map.of("message", "Ownership deleted"));
        } catch (Exception e) {
            log.error("Error deleting ownership: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách users từ ReservationService
     * GET /api/ai/users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            log.info("Fetching users from ReservationService");
            String url = reservationServiceUrl + "/api/users";
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            List<Map<String, Object>> users = response.getBody();
            log.info("Successfully fetched {} users", users != null ? users.size() : 0);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch users: " + e.getMessage()));
        }
    }
    
    /**
     * Health check
     * GET /api/ai/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "AIService",
            "timestamp", LocalDateTime.now()
        ));
    }
}

