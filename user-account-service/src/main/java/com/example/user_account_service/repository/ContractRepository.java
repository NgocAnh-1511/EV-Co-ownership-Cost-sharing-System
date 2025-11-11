package com.example.user_account_service.repository;

import com.example.user_account_service.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    /**
     * Tự động tạo câu lệnh:
     * "SELECT * FROM Contracts WHERE user_id = ?"
     */
    List<Contract> findByUserId(Long userId);

    /**
     * Tự động tạo câu lệnh:
     * "SELECT * FROM Contracts WHERE user_id = ? AND status = ?"
     */
    List<Contract> findByUserIdAndStatus(Long userId, String status);
}