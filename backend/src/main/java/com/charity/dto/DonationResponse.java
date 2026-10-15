package com.charity.dto;

import com.charity.entity.Donation;
import java.time.LocalDateTime;

public class DonationResponse {
    private final Long id;
    private final Long userId;
    private final String username;
    private final Long projectId;
    private final String projectTitle;
    private final String amount;
    private final String requestId;
    private final LocalDateTime createdAt;

    public DonationResponse(Donation donation) {
        id = donation.getId();
        userId = donation.getUser().getId();
        username = donation.getUser().getUsername();
        projectId = donation.getProject().getId();
        projectTitle = donation.getProject().getTitle();
        amount = donation.getAmount().setScale(2).toPlainString();
        requestId = donation.getRequestId();
        createdAt = donation.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Long getProjectId() { return projectId; }
    public String getProjectTitle() { return projectTitle; }
    public String getAmount() { return amount; }
    public String getRequestId() { return requestId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
