package com.example.dispute_management_service.service;

import com.example.dispute_management_service.dto.CreateDisputeRequest; // <-- THÊM IMPORT DTO
import com.example.dispute_management_service.entity.Dispute;
import com.example.dispute_management_service.repository.DisputeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DisputeService {

    @Autowired
    private DisputeRepository disputeRepository;

    /**
     * Lấy tất cả các tranh chấp (cho Admin)
     */
    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    /**
     * Lấy tất cả tranh chấp do một người dùng cụ thể tạo ra.
     */
    public List<Dispute> getDisputesByUserId(Long userId) {
        return disputeRepository.findByCreatedByUserId(userId);
    }

    /**
     * Lấy chi tiết một tranh chấp
     */
    public Optional<Dispute> getDisputeById(Long disputeId) {
        return disputeRepository.findById(disputeId);
    }

    /**
     * NÂNG CẤP: Tạo một tranh chấp mới (sử dụng DTO)
     * @param request Dữ liệu thô từ Frontend
     * @param userId ID người dùng lấy từ Token
     * @return Tranh chấp đã lưu
     */
    public Dispute createDispute(CreateDisputeRequest request, Long userId) {
        Dispute dispute = new Dispute();

        // Lấy dữ liệu từ DTO
        dispute.setContractId(request.getContractId());
        dispute.setAccusedUserId(request.getAccusedUserId());
        dispute.setSubject(request.getSubject());
        dispute.setDescription(request.getDescription());

        // Lấy dữ liệu từ Bảo mật
        dispute.setCreatedByUserId(userId);

        // (Trạng thái và createdAt sẽ được gán tự động bởi @PrePersist)

        return disputeRepository.save(dispute);
    }

    /**
     * Cập nhật trạng thái của một tranh chấp (Dùng cho Admin)
     */
    public Optional<Dispute> updateDisputeStatus(Long disputeId, String newStatus) {
        Optional<Dispute> optionalDispute = disputeRepository.findById(disputeId);

        if (optionalDispute.isPresent()) {
            Dispute dispute = optionalDispute.get();
            dispute.setStatus(newStatus);

            if (newStatus.equals("RESOLVED") || newStatus.equals("CLOSED")) {
                dispute.setResolvedAt(Timestamp.from(Instant.now()));
            }

            return Optional.of(disputeRepository.save(dispute));
        } else {
            return Optional.empty(); // Không tìm thấy
        }
    }
}