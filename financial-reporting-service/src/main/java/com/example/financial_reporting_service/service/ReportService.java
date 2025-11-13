package com.example.financial_reporting_service.service;

import com.example.financial_reporting_service.client.ExternalDataClient;
import com.example.financial_reporting_service.dto.FinancialReportDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ExternalDataClient dataClient;

    public FinancialReportDTO generateGroupFinancialReport(String groupId, String token) {
        FinancialReportDTO report = new FinancialReportDTO();
        report.setGroupId(groupId);
        report.setReportGeneratedAt(Instant.now().toString());

        // 1. Lấy thông tin Nhóm (Từ Vehicle Service 8082)
        JsonNode groupInfo = dataClient.getVehicleGroupByGroupId(groupId, token);
        report.setGroupInfo(groupInfo);

        // 2. Lấy thông tin Xe (Từ Vehicle Service 8082)
        JsonNode vehicleInfo = dataClient.getVehicleByGroupId(groupId, token);
        report.setVehicleInfo(vehicleInfo);

        String vehicleId = null;
        if (vehicleInfo != null && vehicleInfo.has("vehicleId")) {
            vehicleId = vehicleInfo.get("vehicleId").asText();
        }

        // 3. Lấy thông tin Tỷ lệ (Từ User Service 8081)
        if (vehicleId != null) {
            JsonNode shares = dataClient.getOwnershipSharesByVehicleId(vehicleId, token);
            report.setOwnershipShares(convertJsonNodeToList(shares));

            // (Chúng ta có thể lấy danh sách thành viên từ ownership shares)
            // Tạm thời để trống phần members
        }

        // 4. Lấy thông tin Quỹ (Từ Cost Service 8087)
        report.setGroupFund(dataClient.getGroupFund(groupId, token));
        report.setFundTransactions(convertJsonNodeToList(dataClient.getFundTransactions(groupId, token)));

        // 5. Lấy thông tin Chi phí (Từ Cost Service 8087)
        report.setCosts(convertJsonNodeToList(dataClient.getCosts(groupId, token)));
        report.setCostShares(convertJsonNodeToList(dataClient.getCostShares(groupId, token)));

        // 6. Lấy thông tin Thanh toán (Từ Cost Service 8087)
        report.setPayments(convertJsonNodeToList(dataClient.getPayments(groupId, token)));

        // 7. Lấy thông tin Sử dụng (Từ Cost Service 8087)
        report.setUsageTrackings(convertJsonNodeToList(dataClient.getUsageTracking(groupId, token)));

        return report;
    }

    // Helper để chuyển đổi JsonNode (mảng) sang List<JsonNode>
    private List<JsonNode> convertJsonNodeToList(JsonNode node) {
        List<JsonNode> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode element : node) {
                list.add(element);
            }
        }
        return list;
    }
}