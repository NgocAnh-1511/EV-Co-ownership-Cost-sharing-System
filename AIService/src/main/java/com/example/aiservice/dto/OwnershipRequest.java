package com.example.aiservice.dto;

import lombok.*;
import jakarta.validation.constraints.*;

/**
 * Request để thêm/cập nhật thông tin sở hữu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnershipRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;
    
    @NotNull(message = "Group ID is required")
    private Long groupId;
    
    @NotNull(message = "Ownership percentage is required")
    @Min(value = 0, message = "Ownership percentage must be >= 0")
    @Max(value = 100, message = "Ownership percentage must be <= 100")
    private Double ownershipPercentage;
    
    private String role; // ADMIN or MEMBER
}




