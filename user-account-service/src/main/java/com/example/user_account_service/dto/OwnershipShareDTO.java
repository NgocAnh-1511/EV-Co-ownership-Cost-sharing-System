package com.example.user_account_service.dto;

import com.example.user_account_service.entity.OwnershipShare;

public class OwnershipShareDTO {
    private OwnershipShare share;
    private String userFullName;

    public OwnershipShareDTO(OwnershipShare share, String userFullName) {
        this.share = share;
        this.userFullName = userFullName;
    }

    // Getters
    public OwnershipShare getShare() { return share; }
    public String getUserFullName() { return userFullName; }
}