package com.example.financial_reporting_service.controller;

import com.example.financial_reporting_service.dto.FinancialReportDTO;
import com.example.financial_reporting_service.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:8080")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * API để tạo báo cáo tài chính minh bạch cho một nhóm cụ thể.
     * Yêu cầu JWT Token trong Header.
     *
     * URL: GET http://localhost:8088/api/reports/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupFinancialReport(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Trích xuất token (Loại bỏ "Bearer ")
            String token = authHeader.substring(7);

            FinancialReportDTO report = reportService.generateGroupFinancialReport(groupId, token);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            // Nếu một trong các dịch vụ con thất bại, RestTemplate sẽ ném ra lỗi
            return ResponseEntity.status(500)
                    .body("Lỗi khi tổng hợp báo cáo: " + e.getMessage());
        }
    }
}