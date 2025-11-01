package com.example.LegalContractService.service;

import com.example.LegalContractService.model.Legalcontract;
import com.example.LegalContractService.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContractService {

    private final ContractRepository ContractRepository;

    @Autowired
    public ContractService(ContractRepository ContractRepository) {
        this.ContractRepository = ContractRepository;
    }

    public List<Legalcontract> getAllContracts() {
        return ContractRepository.findAll();
    }

    public Optional<Legalcontract> getContractById(String contractId) {
        return ContractRepository.findById(contractId);
    }

    public Legalcontract saveContract(Legalcontract legalcontract) {
        return ContractRepository.save(legalcontract);
    }

    public void deleteContract(String contractId) {
        ContractRepository.deleteById(contractId);
    }

}
