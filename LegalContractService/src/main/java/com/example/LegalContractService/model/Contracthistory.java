package com.example.LegalContractService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ContractHistory", schema = "legal_contract")
public class Contracthistory {
    @Id
    @Column(name = "history_id", nullable = false, length = 20)
    private String historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Legalcontract contract;

    @Size(max = 255)
    @Column(name = "action")
    private String action;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "action_date")
    private Instant actionDate;

}