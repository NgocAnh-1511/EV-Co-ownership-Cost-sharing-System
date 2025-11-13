package com.example.financial_reporting_service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import java.util.List;

@Data
public class FinancialReportDTO {

    // Thông tin cơ bản
    private String groupId;
    private String reportGeneratedAt;
    private JsonNode groupInfo; // Thông tin nhóm từ VehicleService
    private JsonNode vehicleInfo; // Thông tin xe từ VehicleService

    // Thành viên và Tỷ lệ
    private List<JsonNode> members; // Danh sách UserDTO từ UserAccountService
    private List<JsonNode> ownershipShares; // Danh sách OwnershipShareDTO từ UserAccountService

    // Thông tin Quỹ (Từ CostPaymentService)
    private JsonNode groupFund; // Thông tin GroupFundDTO
    private List<JsonNode> fundTransactions; // Danh sách FundTransaction

    // Chi phí (Từ CostPaymentService)
    private List<JsonNode> costs; // Danh sách CostDTO
    private List<JsonNode> costShares; // Danh sách CostShareDTO (chi phí đã chia)

    // Thanh toán (Từ CostPaymentService)
    private List<JsonNode> payments; // Danh sách PaymentDTO (thanh toán của thành viên)

    // Sử dụng (Từ CostPaymentService)
    private List<JsonNode> usageTrackings; // Danh sách UsageTrackingDTO
}