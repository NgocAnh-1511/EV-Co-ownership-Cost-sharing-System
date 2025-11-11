package com.example.dispute_management_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDisputeRequest {
    private Long contractId;
    private Long accusedUserId; // (ID người bị khiếu nại, nếu có)
    private String subject;
    private String description;
}