package com.charity.dto;

import com.charity.entity.CharityProject;
import com.charity.entity.ProjectStatus;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProjectResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String targetAmount;
    private final ProjectStatus status;
    private final LocalDateTime createdAt;
    private final String raisedAmount;
    private final long donationCount;
    private final String progressPercent;

    public ProjectResponse(CharityProject project) {
        this(project, BigDecimal.ZERO, 0);
    }

    public ProjectResponse(CharityProject project, BigDecimal raisedAmount, long donationCount) {
        this.id = project.getId();
        this.title = project.getTitle();
        this.description = project.getDescription();
        this.targetAmount = project.getTargetAmount().toPlainString();
        this.status = project.getStatus();
        this.createdAt = project.getCreatedAt();
        this.raisedAmount = raisedAmount.setScale(2).toPlainString();
        this.donationCount = donationCount;
        this.progressPercent = raisedAmount.multiply(new BigDecimal("100"))
                .divide(project.getTargetAmount(), 2, RoundingMode.HALF_UP).toPlainString();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTargetAmount() { return targetAmount; }
    public ProjectStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getRaisedAmount() { return raisedAmount; }
    public long getDonationCount() { return donationCount; }
    public String getProgressPercent() { return progressPercent; }
}
