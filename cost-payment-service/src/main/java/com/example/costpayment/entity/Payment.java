package com.example.costpayment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId")
    private Integer paymentId;
    
    @Column(name = "userId", nullable = false)
    private Integer userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "costId")
    private Cost cost;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private Payment.PaymentMethod method = Payment.PaymentMethod.EWallet;
    
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    @Column(name = "transactionCode", length = 100)
    private String transactionCode;
    
    @Column(name = "paymentDate")
    private LocalDateTime paymentDate = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Payment.PaymentStatus status = Payment.PaymentStatus.Pending;
    
    public enum PaymentMethod {
        EWallet, Banking, Cash
    }
    
    public enum PaymentStatus {
        Pending, Completed, Failed
    }
}