package com.example.aiservice.service;

import com.example.aiservice.dto.OwnershipRequest;
import com.example.aiservice.model.OwnershipInfo;
import com.example.aiservice.repository.OwnershipInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service quản lý thông tin sở hữu
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OwnershipService {
    
    private final OwnershipInfoRepository ownershipRepository;
    
    /**
     * Thêm hoặc cập nhật thông tin sở hữu
     */
    @Transactional
    public OwnershipInfo saveOwnership(OwnershipRequest request) {
        Optional<OwnershipInfo> existingOpt = ownershipRepository
            .findByUserIdAndVehicleId(request.getUserId(), request.getVehicleId());
        
        OwnershipInfo ownership;
        if (existingOpt.isPresent()) {
            // Cập nhật
            ownership = existingOpt.get();
            ownership.setOwnershipPercentage(request.getOwnershipPercentage());
            if (request.getRole() != null) {
                ownership.setRole(OwnershipInfo.Role.valueOf(request.getRole()));
            }
        } else {
            // Tạo mới
            ownership = OwnershipInfo.builder()
                .userId(request.getUserId())
                .vehicleId(request.getVehicleId())
                .groupId(request.getGroupId())
                .ownershipPercentage(request.getOwnershipPercentage())
                .role(request.getRole() != null ? 
                    OwnershipInfo.Role.valueOf(request.getRole()) : 
                    OwnershipInfo.Role.MEMBER)
                .build();
        }
        
        return ownershipRepository.save(ownership);
    }
    
    /**
     * Lấy tất cả owners của một vehicle
     */
    public List<OwnershipInfo> getOwnersByVehicle(Long vehicleId) {
        return ownershipRepository.findByVehicleId(vehicleId);
    }
    
    /**
     * Lấy tất cả owners trong một nhóm
     */
    public List<OwnershipInfo> getOwnersByGroup(Long groupId) {
        return ownershipRepository.findByGroupId(groupId);
    }
    
    /**
     * Lấy thông tin sở hữu của một user với một vehicle
     */
    public Optional<OwnershipInfo> getOwnership(Long userId, Long vehicleId) {
        return ownershipRepository.findByUserIdAndVehicleId(userId, vehicleId);
    }
    
    /**
     * Kiểm tra tổng ownership percentage có hợp lệ không (phải = 100%)
     */
    public boolean validateTotalOwnership(Long groupId, Long vehicleId) {
        List<OwnershipInfo> owners = ownershipRepository
            .findByGroupIdAndVehicleId(groupId, vehicleId);
        
        double total = owners.stream()
            .mapToDouble(OwnershipInfo::getOwnershipPercentage)
            .sum();
        
        // Cho phép sai số nhỏ do floating point
        return Math.abs(total - 100.0) < 0.01;
    }
    
    /**
     * Xóa thông tin sở hữu
     */
    @Transactional
    public void deleteOwnership(Long ownershipId) {
        ownershipRepository.deleteById(ownershipId);
    }
}


















