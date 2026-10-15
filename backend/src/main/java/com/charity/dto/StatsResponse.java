package com.charity.dto;

import com.charity.repository.DonationSummary;

public class StatsResponse {
    private final long projectCount;
    private final long donationCount;
    private final String totalAmount;

    public StatsResponse(long projectCount, DonationSummary summary) {
        this.projectCount = projectCount;
        this.donationCount = summary.getDonationCount();
        this.totalAmount = summary.getTotalAmount().setScale(2).toPlainString();
    }

    public long getProjectCount() { return projectCount; }
    public long getDonationCount() { return donationCount; }
    public String getTotalAmount() { return totalAmount; }
}
