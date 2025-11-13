package com.example.ui_service.service;

import com.example.ui_service.model.ContractDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ContractRestClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://localhost:8082/api/contracts"; // URL của API trong LegalContractService

    // Lấy tất cả hợp đồng từ service
    public List<ContractDTO> getAllContracts() {
        try {
            ContractDTO[] list = restTemplate.getForObject(BASE_URL, ContractDTO[].class);
            return Arrays.asList(list != null ? list : new ContractDTO[0]);
        } catch (Exception e) {
            System.out.println("⚠️ [ContractService] Lỗi khi lấy danh sách hợp đồng: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
