package com.example.aiservice.service;

import com.example.aiservice.dto.*;
import com.example.aiservice.model.*;
import com.example.aiservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service chính cho AI phân tích và gợi ý
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisService {
    
    private final OwnershipInfoRepository ownershipRepository;
    private final UsageAnalysisRepository usageAnalysisRepository;
    private final FairnessScoreRepository fairnessScoreRepository;
    private final AIRecommendationRepository recommendationRepository;
    private final DataSyncService dataSyncService;
    
    /**
     * Phân tích toàn diện sử dụng xe và tạo gợi ý
     */
    @Transactional
    public AnalysisResponse analyzeVehicleUsage(AnalysisRequest request) {
        log.info("Starting analysis for vehicle {} in group {}", 
            request.getVehicleId(), request.getGroupId());
        
        // 1. Lấy thông tin ownership
        List<OwnershipInfo> owners = ownershipRepository
            .findByGroupIdAndVehicleId(request.getGroupId(), request.getVehicleId());
        
        if (owners.isEmpty()) {
            throw new RuntimeException("No ownership information found for this vehicle");
        }
        
        // 2. Lấy reservations từ ReservationService
        List<Map<String, Object>> reservations = dataSyncService.getReservations(
            request.getVehicleId(), 
            request.getPeriodStart(), 
            request.getPeriodEnd()
        );
        
        // 3. Tính toán usage từ reservations
        Map<Long, DataSyncService.UsageData> usageDataMap = 
            dataSyncService.calculateUsageFromReservations(reservations);
        
        // 4. Tính tổng usage để tính percentage
        double totalHours = usageDataMap.values().stream()
            .mapToDouble(u -> u.totalHours)
            .sum();
        
        // 5. Phân tích từng owner
        List<UsageStatsDTO> ownerStats = new ArrayList<>();
        List<UsageAnalysis> usageAnalyses = new ArrayList<>();
        List<FairnessScore> fairnessScores = new ArrayList<>();
        
        for (OwnershipInfo owner : owners) {
            DataSyncService.UsageData usageData = usageDataMap.getOrDefault(
                owner.getUserId(), 
                new DataSyncService.UsageData()
            );
            
            // Tính usage percentage
            double usagePercentage = totalHours > 0 ? 
                (usageData.totalHours / totalHours * 100.0) : 0.0;
            
            // Lưu Usage Analysis
            UsageAnalysis analysis = UsageAnalysis.builder()
                .userId(owner.getUserId())
                .vehicleId(request.getVehicleId())
                .groupId(request.getGroupId())
                .totalHoursUsed(usageData.totalHours)
                .totalKilometers(0.0) // TODO: implement if have tracking
                .bookingCount(usageData.bookingCount)
                .cancellationCount(usageData.cancellationCount)
                .usagePercentage(usagePercentage)
                .costIncurred(0.0) // TODO: implement cost tracking
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .build();
            usageAnalyses.add(analysis);
            
            // Tính Fairness Score
            double difference = usagePercentage - owner.getOwnershipPercentage();
            double fairnessScore = calculateFairnessScore(
                usagePercentage, 
                owner.getOwnershipPercentage()
            );
            FairnessScore.Priority priority = determinePriority(difference);
            
            FairnessScore fairness = FairnessScore.builder()
                .userId(owner.getUserId())
                .vehicleId(request.getVehicleId())
                .groupId(request.getGroupId())
                .ownershipPercentage(owner.getOwnershipPercentage())
                .usagePercentage(usagePercentage)
                .difference(difference)
                .fairnessScore(fairnessScore)
                .priority(priority)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .build();
            fairnessScores.add(fairness);
            
            // Tạo DTO cho response
            Map<String, Object> userInfo = dataSyncService.getUserInfo(owner.getUserId());
            String userName = userInfo != null ? 
                (String) userInfo.get("fullName") : "Unknown";
            
            UsageStatsDTO stats = UsageStatsDTO.builder()
                .userId(owner.getUserId())
                .userName(userName)
                .ownershipPercentage(owner.getOwnershipPercentage())
                .usagePercentage(usagePercentage)
                .totalHoursUsed(usageData.totalHours)
                .totalKilometers(0.0)
                .bookingCount(usageData.bookingCount)
                .cancellationCount(usageData.cancellationCount)
                .costIncurred(0.0)
                .difference(difference)
                .fairnessScore(fairnessScore)
                .priority(priority.name())
                .build();
            ownerStats.add(stats);
        }
        
        // 6. Lưu vào database
        usageAnalysisRepository.saveAll(usageAnalyses);
        fairnessScoreRepository.saveAll(fairnessScores);
        
        // 7. Tạo AI recommendations
        List<AIRecommendation> recommendations = generateRecommendations(
            request.getGroupId(),
            request.getVehicleId(),
            ownerStats,
            request.getPeriodStart(),
            request.getPeriodEnd()
        );
        recommendationRepository.saveAll(recommendations);
        
        // 8. Tính group fairness score
        double groupFairnessScore = fairnessScores.stream()
            .mapToDouble(FairnessScore::getFairnessScore)
            .average()
            .orElse(0.0);
        
        // 9. Lấy vehicle info
        Map<String, Object> vehicleInfo = dataSyncService.getVehicleInfo(request.getVehicleId());
        String vehicleName = vehicleInfo != null ? 
            (String) vehicleInfo.get("vehicleName") : "Unknown Vehicle";
        
        // 10. Tạo summary
        String summary = generateSummary(groupFairnessScore, ownerStats);
        
        // 11. Chuyển đổi recommendations sang DTO
        List<RecommendationDTO> recommendationDTOs = recommendations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return AnalysisResponse.builder()
            .groupId(request.getGroupId())
            .vehicleId(request.getVehicleId())
            .vehicleName(vehicleName)
            .periodStart(request.getPeriodStart())
            .periodEnd(request.getPeriodEnd())
            .analyzedAt(LocalDateTime.now())
            .ownerStats(ownerStats)
            .groupFairnessScore(groupFairnessScore)
            .totalGroupCost(0.0) // TODO: implement
            .recommendations(recommendationDTOs)
            .summary(summary)
            .build();
    }
    
    /**
     * Tính điểm công bằng (0-100)
     * 100 = hoàn toàn công bằng
     */
    private double calculateFairnessScore(double usagePercentage, double ownershipPercentage) {
        if (ownershipPercentage == 0) return 100.0;
        
        double difference = Math.abs(usagePercentage - ownershipPercentage);
        
        // Công thức: score = 100 - (difference * penalty_factor)
        // Penalty factor = 2 nghĩa là mỗi 1% chênh lệch trừ 2 điểm
        double penaltyFactor = 1.5;
        double score = 100.0 - (difference * penaltyFactor);
        
        return Math.max(0.0, Math.min(100.0, score));
    }
    
    /**
     * Xác định mức độ ưu tiên dựa trên chênh lệch
     */
    private FairnessScore.Priority determinePriority(double difference) {
        // difference = usage - ownership
        // Âm = dùng ít hơn quyền -> ưu tiên cao
        // Dương = dùng nhiều hơn quyền -> ưu tiên thấp
        
        if (difference < -10.0) {
            return FairnessScore.Priority.HIGH; // Dùng ít hơn 10% so với quyền
        } else if (difference > 10.0) {
            return FairnessScore.Priority.LOW; // Dùng nhiều hơn 10% so với quyền
        } else {
            return FairnessScore.Priority.NORMAL;
        }
    }
    
    /**
     * Tạo recommendations dựa trên phân tích
     */
    private List<AIRecommendation> generateRecommendations(
            Long groupId, 
            Long vehicleId,
            List<UsageStatsDTO> ownerStats,
            LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        
        List<AIRecommendation> recommendations = new ArrayList<>();
        
        // 1. Kiểm tra imbalance
        for (UsageStatsDTO stats : ownerStats) {
            double diff = stats.getDifference();
            
            if (diff < -15.0) {
                // User đang sử dụng ít hơn rất nhiều so với quyền
                recommendations.add(AIRecommendation.builder()
                    .groupId(groupId)
                    .vehicleId(vehicleId)
                    .type(AIRecommendation.RecommendationType.USAGE_BALANCE)
                    .title(String.format("%s đang sử dụng xe ít hơn quyền sở hữu", 
                        stats.getUserName()))
                    .description(String.format(
                        "%s có quyền sở hữu %.1f%% nhưng chỉ sử dụng %.1f%% (chênh lệch %.1f%%). " +
                        "Nên ưu tiên %s trong các lần đặt lịch tiếp theo.",
                        stats.getUserName(),
                        stats.getOwnershipPercentage(),
                        stats.getUsagePercentage(),
                        diff,
                        stats.getUserName()
                    ))
                    .severity(AIRecommendation.Severity.WARNING)
                    .targetUserId(stats.getUserId())
                    .status(AIRecommendation.Status.ACTIVE)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .build());
            } else if (diff > 15.0) {
                // User đang sử dụng nhiều hơn rất nhiều so với quyền
                recommendations.add(AIRecommendation.builder()
                    .groupId(groupId)
                    .vehicleId(vehicleId)
                    .type(AIRecommendation.RecommendationType.USAGE_BALANCE)
                    .title(String.format("%s đang sử dụng xe nhiều hơn quyền sở hữu", 
                        stats.getUserName()))
                    .description(String.format(
                        "%s có quyền sở hữu %.1f%% nhưng đã sử dụng %.1f%% (chênh lệch +%.1f%%). " +
                        "Các thành viên khác nên được ưu tiên trong các lần đặt lịch tiếp theo.",
                        stats.getUserName(),
                        stats.getOwnershipPercentage(),
                        stats.getUsagePercentage(),
                        diff
                    ))
                    .severity(AIRecommendation.Severity.WARNING)
                    .targetUserId(stats.getUserId())
                    .status(AIRecommendation.Status.ACTIVE)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .build());
            }
            
            // 2. Kiểm tra cancellation rate
            if (stats.getBookingCount() > 0) {
                double cancellationRate = (double) stats.getCancellationCount() / 
                    stats.getBookingCount() * 100.0;
                
                if (cancellationRate > 30.0) {
                    recommendations.add(AIRecommendation.builder()
                        .groupId(groupId)
                        .vehicleId(vehicleId)
                        .type(AIRecommendation.RecommendationType.SCHEDULE_CONFLICT)
                        .title(String.format("%s có tỷ lệ hủy lịch cao", 
                            stats.getUserName()))
                        .description(String.format(
                            "%s đã hủy %d/%d lịch đặt (%.1f%%). " +
                            "Nên lên kế hoạch cẩn thận hơn để tránh ảnh hưởng đến các thành viên khác.",
                            stats.getUserName(),
                            stats.getCancellationCount(),
                            stats.getBookingCount(),
                            cancellationRate
                        ))
                        .severity(AIRecommendation.Severity.INFO)
                        .targetUserId(stats.getUserId())
                        .status(AIRecommendation.Status.ACTIVE)
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .build());
                }
            }
        }
        
        // 3. Gợi ý chung cho nhóm
        double avgFairness = ownerStats.stream()
            .mapToDouble(UsageStatsDTO::getFairnessScore)
            .average()
            .orElse(100.0);
        
        if (avgFairness < 70.0) {
            recommendations.add(AIRecommendation.builder()
                .groupId(groupId)
                .vehicleId(vehicleId)
                .type(AIRecommendation.RecommendationType.GENERAL_ADVICE)
                .title("Mức độ công bằng sử dụng xe cần cải thiện")
                .description(String.format(
                    "Điểm công bằng của nhóm là %.1f/100. " +
                    "Nên tổ chức cuộc họp để thảo luận và điều chỉnh lịch sử dụng " +
                    "sao cho phù hợp hơn với tỷ lệ sở hữu của từng thành viên.",
                    avgFairness
                ))
                .severity(AIRecommendation.Severity.CRITICAL)
                .status(AIRecommendation.Status.ACTIVE)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build());
        } else if (avgFairness >= 90.0) {
            recommendations.add(AIRecommendation.builder()
                .groupId(groupId)
                .vehicleId(vehicleId)
                .type(AIRecommendation.RecommendationType.GENERAL_ADVICE)
                .title("Nhóm đang sử dụng xe rất công bằng")
                .description(String.format(
                    "Điểm công bằng của nhóm là %.1f/100. " +
                    "Tuyệt vời! Mọi người đang sử dụng xe hợp lý và công bằng.",
                    avgFairness
                ))
                .severity(AIRecommendation.Severity.INFO)
                .status(AIRecommendation.Status.ACTIVE)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build());
        }
        
        return recommendations;
    }
    
    /**
     * Tạo summary text
     */
    private String generateSummary(double groupFairnessScore, List<UsageStatsDTO> ownerStats) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("Điểm công bằng chung: %.1f/100. ", groupFairnessScore));
        
        if (groupFairnessScore >= 90.0) {
            sb.append("Nhóm đang sử dụng xe rất công bằng và hợp lý. ");
        } else if (groupFairnessScore >= 70.0) {
            sb.append("Mức độ công bằng ở mức tốt, nhưng vẫn có thể cải thiện. ");
        } else {
            sb.append("Cần cải thiện mức độ công bằng trong sử dụng xe. ");
        }
        
        // Tìm người dùng nhiều nhất và ít nhất
        Optional<UsageStatsDTO> maxUser = ownerStats.stream()
            .max(Comparator.comparing(UsageStatsDTO::getUsagePercentage));
        Optional<UsageStatsDTO> minUser = ownerStats.stream()
            .min(Comparator.comparing(UsageStatsDTO::getUsagePercentage));
        
        if (maxUser.isPresent() && minUser.isPresent()) {
            sb.append(String.format(
                "%s sử dụng nhiều nhất (%.1f%%), %s sử dụng ít nhất (%.1f%%).",
                maxUser.get().getUserName(),
                maxUser.get().getUsagePercentage(),
                minUser.get().getUserName(),
                minUser.get().getUsagePercentage()
            ));
        }
        
        return sb.toString();
    }
    
    /**
     * Gợi ý thời gian đặt lịch phù hợp
     */
    public ScheduleSuggestionResponse suggestSchedule(ScheduleSuggestionRequest request) {
        // Lấy fairness score hiện tại của user
        Optional<FairnessScore> latestScoreOpt = fairnessScoreRepository
            .findFirstByUserIdAndVehicleIdOrderByCalculatedAtDesc(
                request.getUserId(), 
                request.getVehicleId()
            );
        
        ScheduleSuggestionResponse.ScheduleSuggestionResponseBuilder responseBuilder = 
            ScheduleSuggestionResponse.builder()
                .userId(request.getUserId())
                .vehicleId(request.getVehicleId());
        
        if (latestScoreOpt.isEmpty()) {
            // Chưa có dữ liệu phân tích
            return responseBuilder
                .approved(true)
                .reason("Chưa có dữ liệu phân tích. Bạn có thể đặt lịch tự do.")
                .currentPriority("NORMAL")
                .suggestedTimeSlots(List.of())
                .build();
        }
        
        FairnessScore score = latestScoreOpt.get();
        
        responseBuilder
            .currentPriority(score.getPriority().name())
            .currentUsagePercentage(score.getUsagePercentage())
            .ownershipPercentage(score.getOwnershipPercentage());
        
        // Quyết định approve hay không
        if (score.getPriority() == FairnessScore.Priority.LOW) {
            // User đã dùng quá nhiều
            return responseBuilder
                .approved(false)
                .reason(String.format(
                    "Bạn đã sử dụng %.1f%% trong khi quyền sở hữu là %.1f%%. " +
                    "Nên nhường cho các thành viên khác.",
                    score.getUsagePercentage(),
                    score.getOwnershipPercentage()
                ))
                .suggestedTimeSlots(List.of())
                .build();
        }
        
        // Tạo gợi ý thời gian
        List<ScheduleSuggestionResponse.TimeSlot> slots = new ArrayList<>();
        LocalDateTime baseTime = request.getDesiredStartTime();
        
        slots.add(ScheduleSuggestionResponse.TimeSlot.builder()
            .startTime(baseTime)
            .endTime(baseTime.plusHours(request.getEstimatedHours().longValue()))
            .description("Thời gian bạn yêu cầu")
            .priority(1)
            .build());
        
        String reason;
        if (score.getPriority() == FairnessScore.Priority.HIGH) {
            reason = String.format(
                "Bạn có mức ưu tiên CAO. Bạn mới sử dụng %.1f%% trong khi có quyền %.1f%%. " +
                "Hệ thống sẽ ưu tiên lịch của bạn.",
                score.getUsagePercentage(),
                score.getOwnershipPercentage()
            );
        } else {
            reason = "Bạn có thể đặt lịch bình thường.";
        }
        
        return responseBuilder
            .approved(true)
            .reason(reason)
            .suggestedTimeSlots(slots)
            .build();
    }
    
    /**
     * Lấy tất cả recommendations (cho admin)
     */
    public List<RecommendationDTO> getAllRecommendations(String type, String priority) {
        List<AIRecommendation> recommendations;
        
        if (type != null && !type.isEmpty() && !type.equals("ALL")) {
            try {
                AIRecommendation.RecommendationType recType = 
                    AIRecommendation.RecommendationType.valueOf(type);
                recommendations = recommendationRepository.findByType(recType);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid recommendation type: {}", type);
                recommendations = recommendationRepository.findAll();
            }
        } else if (priority != null && !priority.isEmpty() && !priority.equals("ALL")) {
            try {
                AIRecommendation.Severity severity = 
                    AIRecommendation.Severity.valueOf(priority);
                recommendations = recommendationRepository.findBySeverity(severity);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid severity: {}", priority);
                recommendations = recommendationRepository.findAll();
            }
        } else {
            recommendations = recommendationRepository.findAll();
        }
        
        return recommendations.stream()
            .map(this::convertToDTO)
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy recommendations cho một user cụ thể
     */
    public List<RecommendationDTO> getRecommendationsByUser(Long userId, int limit) {
        List<AIRecommendation> recommendations = recommendationRepository
            .findByTargetUserIdAndStatus(userId, AIRecommendation.Status.ACTIVE);
        
        return recommendations.stream()
            .sorted((a, b) -> {
                // Sort by severity first (CRITICAL > WARNING > INFO)
                int severityCompare = b.getSeverity().compareTo(a.getSeverity());
                if (severityCompare != 0) return severityCompare;
                // Then by created date (newest first)
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            })
            .limit(limit)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy recommendations cho một group
     */
    public List<RecommendationDTO> getRecommendations(Long groupId, String status) {
        AIRecommendation.Status recommendationStatus = status != null ? 
            AIRecommendation.Status.valueOf(status) : 
            AIRecommendation.Status.ACTIVE;
        
        List<AIRecommendation> recommendations = recommendationRepository
            .findByGroupIdAndStatusOrderBySeverity(groupId, recommendationStatus);
        
        return recommendations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert AIRecommendation entity to DTO
     */
    private RecommendationDTO convertToDTO(AIRecommendation recommendation) {
        String targetUserName = null;
        if (recommendation.getTargetUserId() != null) {
            Map<String, Object> userInfo = dataSyncService
                .getUserInfo(recommendation.getTargetUserId());
            if (userInfo != null) {
                targetUserName = (String) userInfo.get("fullName");
            }
        }
        
        return RecommendationDTO.builder()
            .recommendationId(recommendation.getRecommendationId())
            .type(recommendation.getType().name())
            .title(recommendation.getTitle())
            .description(recommendation.getDescription())
            .severity(recommendation.getSeverity().name())
            .targetUserId(recommendation.getTargetUserId())
            .targetUserName(targetUserName)
            .status(recommendation.getStatus().name())
            .createdAt(recommendation.getCreatedAt())
            .readAt(recommendation.getReadAt())
            .build();
    }
    
    /**
     * Đánh dấu recommendation đã đọc
     */
    @Transactional
    public void markRecommendationAsRead(Long recommendationId) {
        AIRecommendation recommendation = recommendationRepository
            .findById(recommendationId)
            .orElseThrow(() -> new RuntimeException("Recommendation not found"));
        
        recommendation.setStatus(AIRecommendation.Status.READ);
        recommendation.setReadAt(LocalDateTime.now());
        recommendationRepository.save(recommendation);
    }
}

