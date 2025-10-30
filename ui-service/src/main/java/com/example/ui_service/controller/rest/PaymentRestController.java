package com.example.ui_service.controller.rest;

import com.example.ui_service.client.CostPaymentClient;
import com.example.ui_service.dto.PaymentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Payment operations (proxy to backend)
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    @Value("${microservices.cost-payment.url:http://localhost:8081}")
    private String costPaymentUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Get all payments
     * GET /api/payments
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllPayments() {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                costPaymentUrl + "/api/payments",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.err.println("Error fetching payments: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get payments by user ID
     * GET /api/payments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getPaymentsByUserId(@PathVariable Integer userId) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                costPaymentUrl + "/api/payments/user/" + userId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.err.println("Error fetching user payments: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get pending payments by user ID
     * GET /api/payments/user/{userId}/pending
     */
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingPaymentsByUserId(@PathVariable Integer userId) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                costPaymentUrl + "/api/payments/user/" + userId + "/pending",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.err.println("Error fetching pending payments: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get payment history by user ID
     * GET /api/payments/user/{userId}/history
     */
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<Map<String, Object>>> getPaymentHistoryByUserId(@PathVariable Integer userId) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                costPaymentUrl + "/api/payments/user/" + userId + "/history",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.err.println("Error fetching payment history: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Create payment
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, Object> payment) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                costPaymentUrl + "/api/payments",
                HttpMethod.POST,
                new HttpEntity<>(payment),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.err.println("Error creating payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi tạo thanh toán: " + e.getMessage()
            ));
        }
    }

    /**
     * Process payment (mark as PAID)
     * POST /api/payments/{id}/process
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<Map<String, Object>> processPayment(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                costPaymentUrl + "/api/payments/" + id + "/process",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi xử lý thanh toán: " + e.getMessage()
            ));
        }
    }
}

