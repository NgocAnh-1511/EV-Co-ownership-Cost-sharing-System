package com.example.ui_service.model;

import java.time.LocalDateTime;

public class CheckinoutDTO {
    private String checkinoutId;
    private String contractId;
    private String vehicleId;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private String status;
    private LocalDateTime qrScanTime;
    private LocalDateTime signatureTime;
    private String notes;
    private String performedBy;
    private String qrCodeData;
    private String signatureImageUrl;
    private String vehicleConditionAfter;
    private String vehicleConditionBefore;

    // Getters & Setters
    public String getCheckinoutId() { return checkinoutId; }
    public void setCheckinoutId(String checkinoutId) { this.checkinoutId = checkinoutId; }

    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public LocalDateTime getCheckinTime() { return checkinTime; }
    public void setCheckinTime(LocalDateTime checkinTime) { this.checkinTime = checkinTime; }

    public LocalDateTime getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(LocalDateTime checkoutTime) { this.checkoutTime = checkoutTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getQrScanTime() { return qrScanTime; }
    public void setQrScanTime(LocalDateTime qrScanTime) { this.qrScanTime = qrScanTime; }

    public LocalDateTime getSignatureTime() { return signatureTime; }
    public void setSignatureTime(LocalDateTime signatureTime) { this.signatureTime = signatureTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }

    public String getSignatureImageUrl() { return signatureImageUrl; }
    public void setSignatureImageUrl(String signatureImageUrl) { this.signatureImageUrl = signatureImageUrl; }

    public String getVehicleConditionAfter() { return vehicleConditionAfter; }
    public void setVehicleConditionAfter(String vehicleConditionAfter) { this.vehicleConditionAfter = vehicleConditionAfter; }

    public String getVehicleConditionBefore() { return vehicleConditionBefore; }
    public void setVehicleConditionBefore(String vehicleConditionBefore) { this.vehicleConditionBefore = vehicleConditionBefore; }
}
