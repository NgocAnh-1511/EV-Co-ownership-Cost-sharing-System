package com.example.LegalContractService.repository;

import com.example.LegalContractService.model.Contracthistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractHistoryRepository extends JpaRepository<Contracthistory, String> {
    List<Contracthistory> findByContract_ContractId(String contractId);
}

