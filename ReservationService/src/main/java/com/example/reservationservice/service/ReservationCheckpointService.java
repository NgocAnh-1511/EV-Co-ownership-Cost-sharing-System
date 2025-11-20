package com.example.reservationservice.service;

import com.example.reservationservice.dto.CheckpointDTO;
import com.example.reservationservice.dto.CheckpointIssueRequest;
import com.example.reservationservice.dto.CheckpointScanRequest;
import com.example.reservationservice.dto.CheckpointSignRequest;
import com.example.reservationservice.model.Reservation;
import com.example.reservationservice.model.ReservationCheckpoint;
import com.example.reservationservice.model.ReservationCheckpoint.CheckpointStatus;
import com.example.reservationservice.model.ReservationCheckpoint.CheckpointType;
import com.example.reservationservice.repository.ReservationCheckpointRepository;
import com.example.reservationservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationCheckpointService {

    private final ReservationRepository reservationRepository;
    private final ReservationCheckpointRepository checkpointRepository;

    @Transactional
    public CheckpointDTO issueCheckpoint(Long reservationId, CheckpointIssueRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        String typeValue = request.getType() != null ? request.getType() : "CHECK_IN";
        CheckpointType type = CheckpointType.valueOf(typeValue.toUpperCase());

        // expire previous pending checkpoints for same type
        List<ReservationCheckpoint> actives = checkpointRepository
                .findActiveByReservationAndType(reservationId, type, List.of(
                        CheckpointStatus.PENDING, CheckpointStatus.SCANNED, CheckpointStatus.SIGNED
                ));
        actives.forEach(cp -> {
            cp.setStatus(CheckpointStatus.EXPIRED);
            cp.setExpiresAt(LocalDateTime.now());
        });
        checkpointRepository.saveAll(actives);

        ReservationCheckpoint checkpoint = new ReservationCheckpoint();
        checkpoint.setReservation(reservation);
        checkpoint.setCheckpointType(type);
        checkpoint.setStatus(CheckpointStatus.PENDING);
        checkpoint.setQrToken(UUID.randomUUID().toString());
        checkpoint.setIssuedBy(request.getIssuedBy() != null ? request.getIssuedBy() : "ADMIN");
        checkpoint.setNotes(request.getNotes());
        checkpoint.setExpiresAt(LocalDateTime.now().plusMinutes(
                request.getValidMinutes() != null ? Math.max(5, request.getValidMinutes()) : 15
        ));

        checkpointRepository.save(checkpoint);
        return toDto(checkpoint, true);
    }

    @Transactional
    public CheckpointDTO scanCheckpoint(CheckpointScanRequest request) {
        ReservationCheckpoint checkpoint = checkpointRepository.findByQrToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("QR token không hợp lệ"));

        if (checkpoint.getExpiresAt() != null && checkpoint.getExpiresAt().isBefore(LocalDateTime.now())) {
            checkpoint.setStatus(CheckpointStatus.EXPIRED);
            checkpointRepository.save(checkpoint);
            throw new IllegalStateException("QR đã hết hạn, vui lòng yêu cầu mã mới.");
        }

        checkpoint.setLatitude(request.getLatitude());
        checkpoint.setLongitude(request.getLongitude());
        checkpoint.setScannedAt(LocalDateTime.now());

        if (checkpoint.getStatus() == CheckpointStatus.PENDING) {
            checkpoint.setStatus(CheckpointStatus.SCANNED);
        }

        checkpointRepository.save(checkpoint);
        return toDto(checkpoint, false);
    }

    @Transactional
    public CheckpointDTO signCheckpoint(Long checkpointId, CheckpointSignRequest request) {
        ReservationCheckpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new IllegalArgumentException("Checkpoint không tồn tại"));

        if (checkpoint.getStatus() == CheckpointStatus.EXPIRED) {
            throw new IllegalStateException("Checkpoint đã hết hạn.");
        }

        checkpoint.setSignerName(request.getSignerName());
        checkpoint.setSignerIdNumber(request.getSignerIdNumber());
        checkpoint.setSignatureData(request.getSignatureData());
        checkpoint.setSignedAt(LocalDateTime.now());
        checkpoint.setStatus(CheckpointStatus.COMPLETED);

        checkpointRepository.save(checkpoint);
        return toDto(checkpoint, false);
    }

    @Transactional(readOnly = true)
    public List<CheckpointDTO> getCheckpointsForReservation(Long reservationId) {
        return checkpointRepository.findByReservation_ReservationIdOrderByIssuedAtDesc(reservationId)
                .stream()
                .map(cp -> toDto(cp, false))
                .collect(Collectors.toList());
    }

    private CheckpointDTO toDto(ReservationCheckpoint checkpoint, boolean includePayload) {
        return CheckpointDTO.builder()
                .checkpointId(checkpoint.getCheckpointId())
                .reservationId(checkpoint.getReservation().getReservationId())
                .checkpointType(checkpoint.getCheckpointType())
                .status(checkpoint.getStatus())
                .qrToken(checkpoint.getQrToken())
                .qrPayload(includePayload ? buildQrPayload(checkpoint) : null)
                .issuedBy(checkpoint.getIssuedBy())
                .issuedAt(checkpoint.getIssuedAt())
                .expiresAt(checkpoint.getExpiresAt())
                .scannedAt(checkpoint.getScannedAt())
                .signedAt(checkpoint.getSignedAt())
                .signerName(checkpoint.getSignerName())
                .signerIdNumber(checkpoint.getSignerIdNumber())
                .signatureData(checkpoint.getSignatureData())
                .latitude(checkpoint.getLatitude())
                .longitude(checkpoint.getLongitude())
                .notes(checkpoint.getNotes())
                .build();
    }

    private String buildQrPayload(ReservationCheckpoint checkpoint) {
        String json = String.format(
                "{\"checkpointId\":%d,\"token\":\"%s\",\"type\":\"%s\",\"reservationId\":%d}",
                checkpoint.getCheckpointId(),
                checkpoint.getQrToken(),
                checkpoint.getCheckpointType(),
                checkpoint.getReservation().getReservationId()
        );
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unused")
    private String hashSignature(String signatureData, String seed) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update((signatureData + seed).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot hash signature", e);
        }
    }
}

