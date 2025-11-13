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
    @Size(max = 20)
    @Column(name = "contract_id", nullable = false, length = 20)
    private String contractId;

    @Size(max = 20)
    @Column(name = "group_id", length = 20)
    private String groupId;

    @Size(max = 100)
    @Column(name = "contract_code", length = 100)
    private String contractCode;

    @Size(max = 50)
    @Column(name = "contract_status", length = 50)
    private String contractStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creation_date")
    private Instant creationDate;

    @Column(name = "signed_date")
    private Instant signedDate;

    @Size(max = 50)
    @Column(name = "contract_type", length = 50)
    private String contractType;

    @Lob
    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "parties")
    private String parties;

    @Lob
    @Column(name = "signature_data")
    private String signatureData;

    @Size(max = 20)
    @Column(name = "signer_id", length = 20)
    private String signerId;

}