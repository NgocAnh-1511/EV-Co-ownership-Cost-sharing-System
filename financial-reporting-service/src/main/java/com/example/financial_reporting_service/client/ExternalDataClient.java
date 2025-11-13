package com.example.financial_reporting_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExternalDataClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${service.url.user-account}")
    private String USER_SERVICE_URL;

    @Value("${service.url.vehicle}")
    private String VEHICLE_SERVICE_URL;

    @Value("${service.url.cost-payment}")
    private String COST_SERVICE_URL;

    // Helper tạo Headers chứa JWT
    private HttpEntity<String> createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(headers);
    }

    // --- Gọi Vehicle Service (8082) ---
    public JsonNode getVehicleGroupByGroupId(String groupId, String token) {
        String url = VEHICLE_SERVICE_URL + "/api/vehicle-groups/" + groupId;
        // Dịch vụ này đang public, không cần token (dựa trên SecurityConfig của nó)
        return restTemplate.getForObject(url, JsonNode.class);
    }

    public JsonNode getVehicleByGroupId(String groupId, String token) {
        String url = VEHICLE_SERVICE_URL + "/api/vehicle-groups/" + groupId + "/vehicles";
        // Dịch vụ này đang public, không cần token
        // Nó trả về 1 List, ta lấy phần tử đầu tiên
        JsonNode vehicles = restTemplate.getForObject(url, JsonNode.class);
        if (vehicles != null && vehicles.isArray() && vehicles.size() > 0) {
            return vehicles.get(0);
        }
        return null;
    }

    // --- Gọi User Account Service (8081) ---
    public JsonNode getOwnershipSharesByVehicleId(String vehicleId, String token) {
        String url = USER_SERVICE_URL + "/api/ownerships?vehicleId=" + vehicleId;
        HttpEntity<String> entity = createAuthHeaders(token);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        return response.getBody();
    }

    // (Giả sử bạn có API lấy user theo group ID, nếu không, chúng ta phải lấy user từ ownership shares)
    // Tạm thời bỏ qua
    // public JsonNode getGroupMembers(String groupId, String token) { ... }


    // --- Gọi Cost Payment Service (8087) ---

    public JsonNode getGroupFund(String groupId, String token) {
        String url = COST_SERVICE_URL + "/api/funds/group/" + groupId;
        HttpEntity<String> entity = createAuthHeaders(token);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        return response.getBody();
    }

    public JsonNode getFundTransactions(String groupId, String token) {
        String url = COST_SERVICE_URL + "/api/funds/transactions/group/" + groupId;
        HttpEntity<String> entity = createAuthHeaders(token);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        return response.getBody();
    }

    public JsonNode getCosts(String groupId, String token) {
        String url = COST_SERVICE_URL + "/api/costs/group/" + groupId;
        HttpEntity<String> entity = createAuthHeaders(token);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        return response.getBody();
    }

    public JsonNode getCostShares(String groupId, String token) {
        // Giả sử API này tồn tại để lấy tất cả cost shares của nhóm
        String url = COST_SERVICE_URL + "/api/cost-shares/group/" + groupId;
        HttpEntity<String> entity = createAuthHeaders(token);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        return response.getBody();
    }

    public JsonNode getPayments(String groupId, String token) {
        // Giả sử API này tồn tại
        String url = COST_SERVICE_URL + "/api/payments/group/" + groupId;
        HttpEntity<String> entity = createAuthHeaders(token);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        return response.getBody();
    }

    public JsonNode getUsageTracking(String groupId, String token) {
        // Giả sử API này tồn tại
        String url = COST_SERVICE_URL + "/api/usage/group/" + groupId;
        HttpEntity<String> entity = createAuthHeaders(token);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        return response.getBody();
    }
}