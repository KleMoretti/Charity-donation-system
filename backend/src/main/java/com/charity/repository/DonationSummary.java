package com.charity.repository;

import java.math.BigDecimal;

public interface DonationSummary {
    Long getDonationCount();
    BigDecimal getTotalAmount();
}
