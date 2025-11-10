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
@Table(name = "legalcontract", schema = "legal_contract")
public class Legalcontract {
    @Id
    @Column(name = "contract_id", nullable = false, length = 20)
    private String contractId;

    @Column(name = "group_id", length = 20)
    private String groupId;

    @Size(max = 100)
    @Column(name = "contract_code", length = 100)
    private String contractCode;

    @Size(max = 50)
    @Column(name = "contract_type", length = 50)
    private String contractType;

    @Size(max = 50)
    @Column(name = "contract_status", length = 50)
    private String contractStatus;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parties", columnDefinition = "TEXT")
    private String parties; // JSON string để lưu danh sách các bên ký kết

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creation_date")
    private Instant creationDate;

    @Column(name = "signed_date")
    private Instant signedDate;

    @Column(name = "signer_id", length = 20)
    private String signerId;

    @Lob
    @Column(name = "signature_data", columnDefinition = "LONGTEXT")
    private String signatureData; // Base64 encoded signature image

}