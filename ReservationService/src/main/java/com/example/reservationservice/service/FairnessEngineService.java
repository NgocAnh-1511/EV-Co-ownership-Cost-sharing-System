package com.example.reservationservice.service;

import com.example.reservationservice.dto.*;
import com.example.reservationservice.model.GroupMember;
import com.example.reservationservice.model.Reservation;
import com.example.reservationservice.model.Vehicle;
import com.example.reservationservice.repository.GroupMemberRepository;
import com.example.reservationservice.repository.ReservationRepository;
import com.example.reservationservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FairnessEngineService {

    private static final double DIFFERENCE_THRESHOLD = 5.0;

    private final VehicleRepository vehicleRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public FairnessSummaryDTO buildSummary(Long vehicleId, Integer rangeDays) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        String groupId = vehicle.getVehicleGroup().getGroupId();
        List<GroupMember> members = groupMemberRepository.findByGroup_GroupId(groupId);

        LocalDateTime rangeEnd = LocalDateTime.now();
        LocalDateTime rangeStart = rangeEnd.minusDays(rangeDays != null && rangeDays > 0 ? rangeDays : 30);

        List<Reservation> reservations = reservationRepository
                .findByVehicleAndRange(vehicleId, rangeStart, rangeEnd);

        Map<Long, Double> usageHours = new HashMap<>();
        Map<Long, LocalDateTime> lastUsage = new HashMap<>();
        Map<Long, LocalDateTime> nextUsage = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();

        reservations.stream()
                .filter(r -> r.getStartDatetime() != null && r.getEndDatetime() != null)
                .forEach(reservation -> {
                    double hours = calculateDurationHours(reservation.getStartDatetime(), reservation.getEndDatetime());
                    Long userId = reservation.getUser().getUserId();
                    usageHours.merge(userId, hours, Double::sum);

                    if (reservation.getEndDatetime().isBefore(now)) {
                        lastUsage.compute(userId, (k, v) -> {
                            if (v == null || reservation.getEndDatetime().isAfter(v)) {
                                return reservation.getEndDatetime();
                            }
                            return v;
                        });
                    } else if (reservation.getStartDatetime().isAfter(now)) {
                        nextUsage.compute(userId, (k, v) -> {
                            if (v == null || reservation.getStartDatetime().isBefore(v)) {
                                return reservation.getStartDatetime();
                            }
                            return v;
                        });
                    }
                });

        double totalUsageHours = usageHours.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        List<FairnessReservationDTO> reservationDTOs = reservations.stream()
                .map(this::mapReservation)
                .collect(Collectors.toList());

        List<FairnessMemberDTO> memberStats = members.stream()
                .map(member -> buildMemberStats(member, usageHours, totalUsageHours, lastUsage, nextUsage))
                .sorted(Comparator.comparingDouble(FairnessMemberDTO::getDifference))
                .collect(Collectors.toList());

        List<Long> priorityQueue = memberStats.stream()
                .sorted(Comparator
                        .comparing(FairnessMemberDTO::getPriority, this::priorityCompare)
                        .thenComparingDouble(FairnessMemberDTO::getDifference))
                .map(FairnessMemberDTO::getUserId)
                .collect(Collectors.toList());

        double fairnessIndex = memberStats.stream()
                .mapToDouble(FairnessMemberDTO::getFairnessScore)
                .average()
                .orElse(100.0);

        List<FairnessAvailabilityDTO> availabilitySlots = buildAvailabilityWindows(rangeStart, rangeEnd, reservationDTOs);

        return FairnessSummaryDTO.builder()
                .vehicleId(vehicleId)
                .vehicleName(vehicle.getVehicleName())
                .groupId(groupId)
                .groupName(vehicle.getVehicleGroup().getGroupName())
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .generatedAt(LocalDateTime.now())
                .totalUsageHours(totalUsageHours)
                .fairnessIndex(roundTwoDecimals(fairnessIndex))
                .members(memberStats)
                .reservations(reservationDTOs)
                .availability(availabilitySlots)
                .priorityQueue(priorityQueue)
                .build();
    }

    @Transactional(readOnly = true)
    public FairnessSuggestionResponse suggest(Long vehicleId, FairnessSuggestionRequest request) {
        FairnessSummaryDTO summary = buildSummary(vehicleId, 30);
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }

        FairnessMemberDTO applicant = summary.getMembers().stream()
                .filter(m -> Objects.equals(m.getUserId(), request.getUserId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not in vehicle's co-ownership list"));

        LocalDateTime desiredStart = Optional.ofNullable(request.getDesiredStart())
                .orElse(LocalDateTime.now().plusHours(1));
        LocalDateTime desiredEnd = desiredStart.plusMinutes(
                Math.max(1L, Math.round(Optional.ofNullable(request.getDurationHours()).orElse(2.0) * 60))
        );

        List<FairnessReservationDTO> conflicts = summary.getReservations().stream()
                .filter(r -> !Objects.equals(r.getStatus(), Reservation.Status.CANCELLED.name()))
                .filter(r -> overlaps(desiredStart, desiredEnd, r.getStart(), r.getEnd()))
                .collect(Collectors.toList());

        boolean otherMembersNeedPriority = summary.getMembers().stream()
                .filter(m -> !Objects.equals(m.getUserId(), applicant.getUserId()))
                .anyMatch(m -> "HIGH".equalsIgnoreCase(m.getPriority()));

        boolean approved = !"LOW".equalsIgnoreCase(applicant.getPriority())
                || (!otherMembersNeedPriority && conflicts.isEmpty());

        String reason;
        if (!approved && "LOW".equalsIgnoreCase(applicant.getPriority())) {
            reason = "Thành viên khác đang được ưu tiên hơn (chênh lệch sử dụng lớn).";
        } else if (!conflicts.isEmpty()) {
            reason = "Thời gian yêu cầu đang bị trùng với lịch khác.";
        } else {
            reason = applicant.getPriority().equalsIgnoreCase("HIGH")
                    ? "Bạn đang được ưu tiên do sử dụng ít hơn quyền sở hữu."
                    : "Bạn có thể đặt lịch trong khung giờ mong muốn.";
        }

        List<FairnessAvailabilityDTO> recommendations = conflicts.isEmpty()
                ? List.of(FairnessAvailabilityDTO.builder()
                .start(desiredStart)
                .end(desiredEnd)
                .durationHours(roundTwoDecimals(calculateDurationHours(desiredStart, desiredEnd)))
                .label("Khung giờ đề xuất")
                .build())
                : findReplacementSlots(summary.getAvailability(), desiredStart, desiredEnd);

        return FairnessSuggestionResponse.builder()
                .vehicleId(vehicleId)
                .userId(request.getUserId())
                .approved(approved && conflicts.isEmpty())
                .priority(applicant.getPriority())
                .reason(reason)
                .requestedStart(desiredStart)
                .requestedEnd(desiredEnd)
                .applicant(applicant)
                .conflicts(conflicts)
                .recommendations(recommendations)
                .build();
    }

    private double calculateDurationHours(LocalDateTime start, LocalDateTime end) {
        return Math.max(0.25, Duration.between(start, end).toMinutes() / 60.0);
    }

    private FairnessReservationDTO mapReservation(Reservation reservation) {
        return FairnessReservationDTO.builder()
                .reservationId(reservation.getReservationId())
                .vehicleId(reservation.getVehicle().getVehicleId())
                .vehicleName(reservation.getVehicle().getVehicleName())
                .userId(reservation.getUser().getUserId())
                .userName(reservation.getUser().getFullName())
                .start(reservation.getStartDatetime())
                .end(reservation.getEndDatetime())
                .status(reservation.getStatus().name())
                .purpose(reservation.getPurpose())
                .build();
    }

    private FairnessMemberDTO buildMemberStats(GroupMember member,
                                               Map<Long, Double> usageHours,
                                               double totalUsageHours,
                                               Map<Long, LocalDateTime> lastUsage,
                                               Map<Long, LocalDateTime> nextUsage) {
        Long userId = member.getUser().getUserId();
        double hours = usageHours.getOrDefault(userId, 0.0);
        double usagePercentage = totalUsageHours > 0 ? (hours / totalUsageHours) * 100 : 0.0;
        double ownership = Optional.ofNullable(member.getOwnershipPercentage()).orElse(0.0);
        double difference = usagePercentage - ownership;
        double fairnessScore = Math.max(0.0, 100.0 - Math.abs(difference) * 2.0);

        return FairnessMemberDTO.builder()
                .userId(userId)
                .fullName(member.getUser().getFullName())
                .email(member.getUser().getEmail())
                .ownershipPercentage(roundTwoDecimals(ownership))
                .usageHours(roundTwoDecimals(hours))
                .usagePercentage(roundTwoDecimals(usagePercentage))
                .difference(roundTwoDecimals(difference))
                .fairnessScore(roundTwoDecimals(fairnessScore))
                .priority(classifyPriority(difference))
                .lastUsageEnd(lastUsage.get(userId))
                .nextReservationStart(nextUsage.get(userId))
                .build();
    }

    private String classifyPriority(double difference) {
        if (difference <= -DIFFERENCE_THRESHOLD) {
            return "HIGH";
        }
        if (difference >= DIFFERENCE_THRESHOLD) {
            return "LOW";
        }
        return "NORMAL";
    }

    private int priorityCompare(String a, String b) {
        List<String> order = List.of("HIGH", "NORMAL", "LOW");
        return Integer.compare(order.indexOf(a.toUpperCase()), order.indexOf(b.toUpperCase()));
    }

    private List<FairnessAvailabilityDTO> buildAvailabilityWindows(LocalDateTime rangeStart,
                                                                   LocalDateTime rangeEnd,
                                                                   List<FairnessReservationDTO> reservations) {
        List<FairnessAvailabilityDTO> slots = new ArrayList<>();

        List<FairnessReservationDTO> sorted = reservations.stream()
                .sorted(Comparator.comparing(FairnessReservationDTO::getStart))
                .collect(Collectors.toList());

        LocalDateTime cursor = rangeStart;
        for (FairnessReservationDTO reservation : sorted) {
            if (reservation.getStart() == null || reservation.getEnd() == null) {
                continue;
            }
            if (cursor.isBefore(reservation.getStart())) {
                slots.add(buildSlot(cursor, reservation.getStart()));
            }
            if (cursor.isBefore(reservation.getEnd())) {
                cursor = reservation.getEnd();
            }
        }

        if (cursor.isBefore(rangeEnd)) {
            slots.add(buildSlot(cursor, rangeEnd));
        }

        return slots.stream()
                .filter(slot -> slot.getDurationHours() >= 0.25)
                .collect(Collectors.toList());
    }

    private FairnessAvailabilityDTO buildSlot(LocalDateTime start, LocalDateTime end) {
        double hours = roundTwoDecimals(calculateDurationHours(start, end));
        return FairnessAvailabilityDTO.builder()
                .start(start)
                .end(end)
                .durationHours(hours)
                .label(hours >= 1 ? String.format("Trống %.1f giờ", hours) : "Trống ngắn")
                .build();
    }

    private List<FairnessAvailabilityDTO> findReplacementSlots(List<FairnessAvailabilityDTO> availability,
                                                               LocalDateTime desiredStart,
                                                               LocalDateTime desiredEnd) {
        double requestedHours = calculateDurationHours(desiredStart, desiredEnd);
        return availability.stream()
                .filter(slot -> slot.getDurationHours() >= requestedHours)
                .sorted(Comparator.comparing(slot -> Duration.between(desiredStart, slot.getStart()).abs()))
                .limit(3)
                .collect(Collectors.toList());
    }

    private boolean overlaps(LocalDateTime start1, LocalDateTime end1,
                              LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

