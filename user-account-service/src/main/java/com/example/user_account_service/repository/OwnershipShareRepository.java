package com.example.user_account_service.repository;

import com.example.user_account_service.entity.OwnershipShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OwnershipShareRepository extends JpaRepository<OwnershipShare, Long> {

    List<OwnershipShare> findByVehicleId(String vehicleId);

    Optional<OwnershipShare> findByVehicleIdAndUserId(String vehicleId, Long userId);

    @Query("SELECT SUM(o.percentage) FROM OwnershipShare o WHERE o.vehicleId = :vehicleId")
    BigDecimal sumPercentageByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * NÂNG CẤP: Thêm phương thức này
     * Tự động tạo câu lệnh:
     * "SELECT * FROM ownership_share WHERE user_id = ?"
     */
    List<OwnershipShare> findByUserId(Long userId);
}