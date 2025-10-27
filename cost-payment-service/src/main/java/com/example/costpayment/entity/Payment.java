package com.example.costpayment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId")
    private Integer paymentId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "costId")
    private Integer costId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "transactionCode", length = 100)
    private String transactionCode;

    @Column(name = "paymentDate")
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    // Constructors
    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.Pending;
    }

    public Payment(Integer userId, Integer costId, PaymentMethod method, Double amount, String transactionCode) {
        this();
        this.userId = userId;
        this.costId = costId;
        this.method = method;
        this.amount = amount;
        this.transactionCode = transactionCode;
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

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    // PaymentMethod enum
    public enum PaymentMethod {
        EWallet("Ví điện tử"),
        Banking("Chuyển khoản"),
        Cash("Tiền mặt");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // PaymentStatus enum
    public enum PaymentStatus {
        Pending("Chờ xử lý"),
        Completed("Hoàn thành"),
        Failed("Thất bại");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", userId=" + userId +
                ", costId=" + costId +
                ", method=" + method +
                ", amount=" + amount +
                ", transactionCode='" + transactionCode + '\'' +
                ", paymentDate=" + paymentDate +
                ", status=" + status +
                '}';
    }
}