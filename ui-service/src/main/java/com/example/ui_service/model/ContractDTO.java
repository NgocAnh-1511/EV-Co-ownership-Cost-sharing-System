package com.example.ui_service.model;

import java.time.LocalDateTime;

public class ContractDTO {
    private String contractId;
    private String contractCode;
    private String contractStatus;
    private String groupId;
    private LocalDateTime creationDate;
    private LocalDateTime signedDate;
    private String signerId;
    private String lastAction;
    private LocalDateTime lastActionDate;

    // Getters & Setters
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }

    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }

    public String getContractStatus() { return contractStatus; }
    public void setContractStatus(String contractStatus) { this.contractStatus = contractStatus; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public LocalDateTime getSignedDate() { return signedDate; }
    public void setSignedDate(LocalDateTime signedDate) { this.signedDate = signedDate; }

    public String getSignerId() { return signerId; }
    public void setSignerId(String signerId) { this.signerId = signerId; }

    public String getLastAction() { return lastAction; }
    public void setLastAction(String lastAction) { this.lastAction = lastAction; }

    public LocalDateTime getLastActionDate() { return lastActionDate; }
    public void setLastActionDate(LocalDateTime lastActionDate) { this.lastActionDate = lastActionDate; }
}
