package com.example.ui_service.client;

import com.example.ui_service.dto.CostItemDto;
import com.example.ui_service.dto.CostSplitDto;
import com.example.ui_service.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CostPaymentClient {

    private final RestTemplate restTemplate;

    @Value("${microservices.cost-payment.url}")
    private String costPaymentUrl;

    // Cost Management APIs
    public List<CostItemDto> getAllCostItems() {
        ResponseEntity<List<CostItemDto>> response = restTemplate.exchange(
                costPaymentUrl + "/api/costs",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CostItemDto>>() {}
        );
        return response.getBody();
    }

    public List<CostItemDto> getCostItemsByGroup(String groupId) {
        ResponseEntity<List<CostItemDto>> response = restTemplate.exchange(
                costPaymentUrl + "/api/costs/group/" + groupId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CostItemDto>>() {}
        );
        return response.getBody();
    }

    public CostItemDto getCostItemById(Long id) {
        return restTemplate.getForObject(costPaymentUrl + "/api/costs/" + id, CostItemDto.class);
    }

    public CostItemDto createCostItem(CostItemDto costItem) {
        return restTemplate.postForObject(costPaymentUrl + "/api/costs", costItem, CostItemDto.class);
    }

    public CostItemDto updateCostItem(Long id, CostItemDto costItem) {
        restTemplate.put(costPaymentUrl + "/api/costs/" + id, costItem);
        return getCostItemById(id);
    }

    public void deleteCostItem(Long id) {
        restTemplate.delete(costPaymentUrl + "/api/costs/" + id);
    }

    // Cost Split Management APIs
    public List<CostSplitDto> getCostSplits(Long costItemId) {
        ResponseEntity<List<CostSplitDto>> response = restTemplate.exchange(
                costPaymentUrl + "/api/costs/" + costItemId + "/splits",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CostSplitDto>>() {}
        );
        return response.getBody();
    }

    public List<CostSplitDto> createCostSplits(Long costItemId) {
        ResponseEntity<List<CostSplitDto>> response = restTemplate.exchange(
                costPaymentUrl + "/api/costs/" + costItemId + "/splits",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<List<CostSplitDto>>() {}
        );
        return response.getBody();
    }

    // Payment Management APIs
    public List<PaymentDto> getPaymentsByUser(String userId) {
        ResponseEntity<List<PaymentDto>> response = restTemplate.exchange(
                costPaymentUrl + "/api/costs/user/" + userId + "/payments",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PaymentDto>>() {}
        );
        return response.getBody();
    }

    public PaymentDto createPayment(Long splitId, PaymentDto payment) {
        return restTemplate.postForObject(
                costPaymentUrl + "/api/costs/splits/" + splitId + "/payments",
                payment,
                PaymentDto.class
        );
    }

    // Financial Summary APIs
    public Map<String, Object> getGroupFinancialSummary(String groupId) {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                costPaymentUrl + "/api/costs/group/" + groupId + "/summary",
                Map.class
        );
        return response.getBody();
    }

    public Map<String, Object> getUserFinancialSummary(String userId) {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                costPaymentUrl + "/api/costs/user/" + userId + "/summary",
                Map.class
        );
        return response.getBody();
    }
}
