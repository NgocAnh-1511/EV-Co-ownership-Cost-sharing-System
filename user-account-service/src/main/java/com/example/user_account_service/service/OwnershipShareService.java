package com.example.user_account_service.service;

import com.example.user_account_service.dto.OwnershipShareCreateReq;
import com.example.user_account_service.dto.OwnershipShareDTO;
import com.example.user_account_service.dto.OwnershipShareUpdateReq;
import com.example.user_account_service.entity.OwnershipShare;
import com.example.user_account_service.repository.OwnershipShareRepository;
import com.example.user_account_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OwnershipShareService {
    private final OwnershipShareRepository repo;
    private final UserRepository userRepository; // <-- Thêm UserRepository

    public OwnershipShareService(OwnershipShareRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    /**
     * NÂNG CẤP: Trả về DTO chứa tên người dùng
     */
    public List<OwnershipShareDTO> listByVehicle(String vehicleId) {
        return repo.findByVehicleId(vehicleId).stream()
                .map(share -> new OwnershipShareDTO(
                        share,
                        // Tìm tên user
                        userRepository.findById(share.getUserId())
                                .map(user -> user.getFullName())
                                .orElse("Không rõ User")
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public OwnershipShare create(OwnershipShareCreateReq req) {
        // Kiểm tra trùng (vehicleId, userId)
        repo.findByVehicleIdAndUserId(req.getVehicleId(), req.getUserId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("User này đã có tỷ lệ cho vehicle này.");
                });

        // NÂNG CẤP LOGIC: Kiểm tra tổng TRƯỚC KHI LƯU
        validateTotalNotExceeds(req.getVehicleId(), req.getPercentage(), null);

        var entity = new OwnershipShare();
        entity.setVehicleId(req.getVehicleId());
        entity.setUserId(req.getUserId());
        entity.setPercentage(req.getPercentage().setScale(2, RoundingMode.HALF_UP));
        entity.setEffectiveFrom(req.getEffectiveFrom());
        entity.setEffectiveTo(req.getEffectiveTo());
        entity.setCreatedAt(OffsetDateTime.now()); // Gán thời gian tạo
        entity.setUpdatedAt(OffsetDateTime.now());

        return repo.save(entity);
    }

    @Transactional
    public OwnershipShare update(OwnershipShareUpdateReq req) {
        var entity = repo.findById(req.getId()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy record"));

        BigDecimal oldPercentage = entity.getPercentage();
        BigDecimal newPercentage = req.getPercentage().setScale(2, RoundingMode.HALF_UP);

        // NÂNG CẤP LOGIC: Kiểm tra tổng TRƯỚC KHI LƯU
        validateTotalNotExceeds(entity.getVehicleId(), newPercentage, oldPercentage);

        entity.setPercentage(newPercentage);
        entity.setEffectiveFrom(req.getEffectiveFrom());
        entity.setEffectiveTo(req.getEffectiveTo());
        entity.setUpdatedAt(OffsetDateTime.now());

        return repo.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        var entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy record"));
        repo.delete(entity);
        // NÂNG CẤP LOGIC: Không cần validate 100% khi xóa.
    }

    /**
     * NÂNG CẤP: Logic kiểm tra tổng tỷ lệ (chạy TRƯỚC KHI LƯU)
     * Sẽ throw exception NẾU tổng mới VƯỢT QUÁ 100.
     */
    private void validateTotalNotExceeds(String vehicleId, BigDecimal newPercentage, BigDecimal oldPercentage) {
        BigDecimal currentSum = repo.sumPercentageByVehicleId(vehicleId);
        if (currentSum == null) currentSum = BigDecimal.ZERO;

        BigDecimal futureSum;
        if (oldPercentage != null) {
            // Đây là kịch bản UPDATE
            futureSum = currentSum.subtract(oldPercentage).add(newPercentage);
        } else {
            // Đây là kịch bản CREATE
            futureSum = currentSum.add(newPercentage);
        }

        // So sánh (chỉ báo lỗi nếu > 100)
        if (futureSum.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalStateException("Hành động này sẽ làm tổng tỷ lệ vượt quá 100%. Tổng hiện tại: " + currentSum + ", Tổng mới sẽ là: " + futureSum);
        }
    }
}