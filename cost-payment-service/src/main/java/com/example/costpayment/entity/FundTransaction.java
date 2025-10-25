package com.example.costpayment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "FundTransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transactionId")
    private Integer transactionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fundId", nullable = false)
    private GroupFund groupFund;
    
    @Column(name = "userId")
    private Integer userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transactionType")
    private TransactionType transactionType = TransactionType.Deposit;
    
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    @Column(name = "purpose", length = 255)
    private String purpose;
    
    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();
    
    public enum TransactionType {
        Deposit, Withdraw
    }
}
