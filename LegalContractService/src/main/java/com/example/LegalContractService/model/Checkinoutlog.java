package com.example.LegalContractService.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkinoutlog") // đúng với tên bảng trong MySQL
public class Checkinoutlog {

    @Id
    @Column(name = "checkinout_id")
    private String checkinoutId;

    @Column(name = "contract_id")
    private String contractId;

    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    @Column(name = "status")
    private String status;

    @Column(name = "qr_scan_time")
    private LocalDateTime qrScanTime;

    @Column(name = "signature_time")
    private LocalDateTime signatureTime;

    @Column(name = "notes")
    private String notes;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "qr_code_data")
    private String qrCodeData;

    @Column(name = "signature_image_url")
    private String signatureImageUrl;

    @Column(name = "vehicle_condition_after")
    private String vehicleConditionAfter;

    @Column(name = "vehicle_condition_before")
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
