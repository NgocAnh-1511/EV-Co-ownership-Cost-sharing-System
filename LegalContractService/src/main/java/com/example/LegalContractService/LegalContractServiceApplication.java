package com.example.LegalContractService;  // ✅ CHÚ Ý: package phải giống y hệt controller gốc

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.LegalContractService") // ✅ quét toàn bộ controller/service
public class LegalContractServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegalContractServiceApplication.class, args);
        System.out.println("🚗 LegalContractService started on port 8082 ✅");
    }
}
