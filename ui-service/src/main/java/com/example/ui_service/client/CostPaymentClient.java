package com.example.ui_service.client;

import com.example.ui_service.dto.CostDto;
import com.example.ui_service.dto.CostSplitDto;
import com.example.ui_service.dto.PaymentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class CostPaymentClient {

    @Value("${microservices.cost-payment.url:http://localhost:8081}")
    private String costPaymentUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<CostDto> getAllCosts() {
        try {
            ResponseEntity<List<CostDto>> response = restTemplate.exchange(
                costPaymentUrl + "/api/costs",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CostDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching costs: " + e.getMessage());
            return List.of();
        }
    }

    public CostDto createCost(CostDto costDto) {
        try {
            return restTemplate.postForObject(costPaymentUrl + "/api/costs", costDto, CostDto.class);
        } catch (Exception e) {
            System.err.println("Error creating cost: " + e.getMessage());
            return null;
        }
    }

    public List<CostSplitDto> getCostSplits(Integer costId) {
        try {
            ResponseEntity<List<CostSplitDto>> response = restTemplate.exchange(
                costPaymentUrl + "/api/costs/" + costId + "/shares",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CostSplitDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching cost splits: " + e.getMessage());
            return List.of();
        }
    }

    public CostSplitDto createCostSplit(Integer costId, CostSplitDto splitDto) {
        try {
            return restTemplate.postForObject(costPaymentUrl + "/api/costs/" + costId + "/shares", splitDto, CostSplitDto.class);
        } catch (Exception e) {
            System.err.println("Error creating cost split: " + e.getMessage());
            return null;
        }
    }

    public List<PaymentDto> getAllPayments() {
        try {
            ResponseEntity<List<PaymentDto>> response = restTemplate.exchange(
                costPaymentUrl + "/api/costs/payments",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PaymentDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching payments: " + e.getMessage());
            return List.of();
        }
    }

    public PaymentDto createPayment(PaymentDto paymentDto) {
        try {
            return restTemplate.postForObject(costPaymentUrl + "/api/costs/payments", paymentDto, PaymentDto.class);
        } catch (Exception e) {
            System.err.println("Error creating payment: " + e.getMessage());
            return null;
        }
    }

    // Additional CRUD operations for Cost management
    public CostDto getCostById(Integer id) {
        try {
            return restTemplate.getForObject(costPaymentUrl + "/api/costs/" + id, CostDto.class);
        } catch (Exception e) {
            System.err.println("Error fetching cost by ID: " + e.getMessage());
            return null;
        }
    }

    public CostDto updateCost(Integer id, CostDto costDto) {
        try {
            restTemplate.put(costPaymentUrl + "/api/costs/" + id, costDto);
            return getCostById(id);
        } catch (Exception e) {
            System.err.println("Error updating cost: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteCost(Integer id) {
        try {
            restTemplate.delete(costPaymentUrl + "/api/costs/" + id);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting cost: " + e.getMessage());
            return false;
        }
    }

    public List<CostDto> searchCosts(String query, String costType, Integer vehicleId) {
        try {
            StringBuilder url = new StringBuilder(costPaymentUrl + "/api/costs/search?");
            if (query != null && !query.isEmpty()) {
                url.append("query=").append(query).append("&");
            }
            if (costType != null && !costType.isEmpty()) {
                url.append("costType=").append(costType).append("&");
            }
            if (vehicleId != null) {
                url.append("vehicleId=").append(vehicleId).append("&");
            }
            
            ResponseEntity<List<CostDto>> response = restTemplate.exchange(
                url.toString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CostDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            System.err.println("Error searching costs: " + e.getMessage());
            return List.of();
        }
    }
}