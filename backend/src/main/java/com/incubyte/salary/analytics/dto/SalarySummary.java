package com.incubyte.salary.analytics.dto;

import java.math.BigDecimal;

/**
 * totalEmployees counts every employee; the salary figures cover only
 * employees who currently have an effective salary record, normalized to
 * {@code currency}. Null average/median/min/max mean no employee currently
 * has a salary on record.
 */
public record SalarySummary(
        long totalEmployees,
        BigDecimal totalSalaryCost,
        BigDecimal averageSalary,
        BigDecimal medianSalary,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        String currency
) {
}
