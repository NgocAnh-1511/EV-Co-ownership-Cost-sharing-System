package com.example.dispute_management_service.repository;

import com.example.dispute_management_service.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    /**
     * Tự động tạo câu lệnh:
     * "SELECT * FROM Disputes WHERE created_by_user_id = ?"
     */
    List<Dispute> findByCreatedByUserId(Long userId);

    /**
     * Tự động tạo câu lệnh:
     * "SELECT * FROM Disputes WHERE status = ?"
     */
    List<Dispute> findByStatus(String status);

    /**
     * Tự động tạo câu lệnh:
     * "SELECT * FROM Disputes WHERE contract_id = ?"
     */
    List<Dispute> findByContractId(Long contractId);
}