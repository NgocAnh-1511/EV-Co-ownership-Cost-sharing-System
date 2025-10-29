package com.example.ui_service.repository;

import com.example.ui_service.model.Ownership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnershipRepository extends JpaRepository<Ownership, Long> {
    // Các phương thức truy vấn tùy chỉnh nếu cần
}