package com.example.LegalContractService.controller;

import com.example.LegalContractService.model.Legalcontract;
import com.example.LegalContractService.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractAPI {

    @Autowired
    private ContractService contractService;

    // Lấy tất cả các hợp đồng
    @GetMapping
    public List<Legalcontract> getAllContracts() {
        return contractService.getAllContracts();
    }
}
