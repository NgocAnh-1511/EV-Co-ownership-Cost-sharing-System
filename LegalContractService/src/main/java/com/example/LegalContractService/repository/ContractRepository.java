package com.example.LegalContractService.repository;

import com.example.LegalContractService.model.Legalcontract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for LegalContract entity
 */
@Repository
public interface ContractRepository extends JpaRepository<Legalcontract, String> {
    
    /**
     * Find all contracts by group ID
     */
    List<Legalcontract> findByGroupId(String groupId);
    
    /**
     * Find all contracts by status
     */
    List<Legalcontract> findByContractStatus(String contractStatus);
    
    /**
     * Find all contracts by type
     */
    List<Legalcontract> findByContractType(String contractType);
    
    /**
     * Find contracts by status and group ID
     */
    List<Legalcontract> findByContractStatusAndGroupId(String contractStatus, String groupId);
}

