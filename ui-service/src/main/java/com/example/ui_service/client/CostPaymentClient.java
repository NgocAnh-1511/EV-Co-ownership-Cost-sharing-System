package com.example.ui_service.client;

import com.example.ui_service.dto.CostDto;
import com.example.ui_service.dto.CostSplitDto;
import com.example.ui_service.dto.PaymentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class CostPaymentClient {

    @Value("${microservices.cost-payment.url:http://localhost:8083}")
    private String costPaymentUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<CostDto> getAllCosts() {
        try {
            CostDto[] costs = restTemplate.getForObject(costPaymentUrl + "/api/costs", CostDto[].class);
            return costs != null ? Arrays.asList(costs) : List.of();
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
            CostSplitDto[] splits = restTemplate.getForObject(costPaymentUrl + "/api/costs/" + costId + "/shares", CostSplitDto[].class);
            return splits != null ? Arrays.asList(splits) : List.of();
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
            PaymentDto[] payments = restTemplate.getForObject(costPaymentUrl + "/api/costs/payments", PaymentDto[].class);
            return payments != null ? Arrays.asList(payments) : List.of();
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
}