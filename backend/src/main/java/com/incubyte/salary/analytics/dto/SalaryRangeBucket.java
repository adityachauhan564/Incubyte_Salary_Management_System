package com.incubyte.salary.analytics.dto;

import java.math.BigDecimal;

/** One fixed-width salary range, normalized to {@code currency}. rangeEnd is exclusive. */
public record SalaryRangeBucket(
        String label,
        BigDecimal rangeStart,
        BigDecimal rangeEnd,
        long count,
        String currency
) {
}
