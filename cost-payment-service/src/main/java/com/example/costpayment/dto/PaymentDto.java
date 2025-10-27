package com.example.costpayment.dto;

import com.example.costpayment.entity.Payment;
import java.time.LocalDateTime;

public class PaymentDto {
    private Integer paymentId;
    private Integer userId;
    private Integer costId;
    private String method;
    private String methodDisplay;
    private Double amount;
    private String transactionCode;
    private LocalDateTime paymentDate;
    private String status;
    private String statusDisplay;

    // Constructors
    public PaymentDto() {}

    public PaymentDto(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.userId = payment.getUserId();
        this.costId = payment.getCostId();
        this.method = payment.getMethod().name();
        this.methodDisplay = payment.getMethod().getDisplayName();
        this.amount = payment.getAmount();
        this.transactionCode = payment.getTransactionCode();
        this.paymentDate = payment.getPaymentDate();
        this.status = payment.getStatus().name();
        this.statusDisplay = payment.getStatus().getDisplayName();
    }

    // Getters and Setters
    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCostId() {
        return costId;
    }

    public void setCostId(Integer costId) {
        this.costId = costId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethodDisplay() {
        return methodDisplay;
    }

    public void setMethodDisplay(String methodDisplay) {
        this.methodDisplay = methodDisplay;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }

    // Convert to Entity
    public Payment toEntity() {
        Payment payment = new Payment();
        payment.setPaymentId(this.paymentId);
        payment.setUserId(this.userId);
        payment.setCostId(this.costId);
        if (this.method != null) {
            payment.setMethod(Payment.PaymentMethod.valueOf(this.method));
        }
        payment.setAmount(this.amount);
        payment.setTransactionCode(this.transactionCode);
        payment.setPaymentDate(this.paymentDate);
        if (this.status != null) {
            payment.setStatus(Payment.PaymentStatus.valueOf(this.status));
        }
        return payment;
    }

    @Override
    public String toString() {
        return "PaymentDto{" +
                "paymentId=" + paymentId +
                ", userId=" + userId +
                ", costId=" + costId +
                ", method='" + method + '\'' +
                ", methodDisplay='" + methodDisplay + '\'' +
                ", amount=" + amount +
                ", transactionCode='" + transactionCode + '\'' +
                ", paymentDate=" + paymentDate +
                ", status='" + status + '\'' +
                ", statusDisplay='" + statusDisplay + '\'' +
                '}';
    }
}
