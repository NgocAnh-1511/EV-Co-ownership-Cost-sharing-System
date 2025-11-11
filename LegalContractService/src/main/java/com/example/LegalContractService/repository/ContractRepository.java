package com.example.LegalContractService.repository;

import com.example.LegalContractService.model.Legalcontract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Legalcontract, String> {
    // Các phương thức truy vấn có thể được thêm vào nếu cần thiết
    List<Legalcontract> findByGroupId(String groupId);
}
