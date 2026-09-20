package com.incubyte.salary.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * totalEmployees counts every employee; the salary figures cover only
 * employees who currently have an effective salary record, normalized to
 * {@code currency}. Null average/median/min/max mean no employee currently
 * has a salary on record.
 */
@Schema(description = "Org-wide compensation summary, normalized to one reporting currency.")
public record SalarySummary(

        @Schema(description = "Every employee, regardless of whether they have a current salary.", example = "10000")
        long totalEmployees,

        @Schema(description = "Sum of every employee's current salary. 0 if none have one.", example = "950000000.00")
        BigDecimal totalSalaryCost,

        @Schema(description = "Null if no employee currently has a salary.", example = "95000.00")
        BigDecimal averageSalary,

        @Schema(description = "Null if no employee currently has a salary.", example = "92000.00")
        BigDecimal medianSalary,

        @Schema(description = "Null if no employee currently has a salary.", example = "42000.00")
        BigDecimal minSalary,

        @Schema(description = "Null if no employee currently has a salary.", example = "210000.00")
        BigDecimal maxSalary,

        @Schema(description = "Reporting currency all figures are normalized to.", example = "USD")
        String currency
) {
}
