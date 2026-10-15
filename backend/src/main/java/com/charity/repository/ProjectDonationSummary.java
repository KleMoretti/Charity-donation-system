package com.charity.repository;

import java.math.BigDecimal;

public interface ProjectDonationSummary {
    Long getProjectId();
    BigDecimal getRaisedAmount();
    Long getDonationCount();
}
