package com.example.user_account_service.repository;

import com.example.user_account_service.entity.OwnershipShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional; // <-- DÒNG BỊ THIẾU GÂY LỖI

@Repository
public interface OwnershipShareRepository extends JpaRepository<OwnershipShare, Long> {

    /**
     * Tự động tạo câu lệnh:
     * "SELECT * FROM ownership_share WHERE vehicle_id = ?"
     * (Đã cập nhật để dùng String vehicleId)
     */
    List<OwnershipShare> findByVehicleId(String vehicleId);

    /**
     * Tự động tạo câu lệnh:
     * "SELECT * FROM ownership_share WHERE vehicle_id = ? AND user_id = ?"
     */
    Optional<OwnershipShare> findByVehicleIdAndUserId(String vehicleId, Long userId);

    /**
     * Tính tổng tỷ lệ phần trăm cho một xe cụ thể.
     */
    @Query("SELECT SUM(o.percentage) FROM OwnershipShare o WHERE o.vehicleId = :vehicleId")
    BigDecimal sumPercentageByVehicleId(@Param("vehicleId") String vehicleId);
}