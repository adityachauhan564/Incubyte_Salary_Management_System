package com.incubyte.salary.analytics.dto;

import java.math.BigDecimal;

/** Per-department or per-country salary comparison and cost, normalized to {@code currency}. */
public record GroupedSalaryStats(
        String name,
        long headcount,
        BigDecimal totalCost,
        BigDecimal averageSalary,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        String currency
) {
}
