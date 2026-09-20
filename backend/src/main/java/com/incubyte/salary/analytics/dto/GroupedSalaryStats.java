package com.incubyte.salary.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Per-department or per-country salary comparison and cost, normalized to {@code currency}. */
@Schema(description = "Salary comparison and cost for one department or country group.")
public record GroupedSalaryStats(

        @Schema(description = "The department or country name, depending on which endpoint returned this.",
                example = "Engineering")
        String name,

        @Schema(description = "Employees in this group who currently have a salary.", example = "1200")
        long headcount,

        @Schema(example = "114000000.00")
        BigDecimal totalCost,

        @Schema(example = "95000.00")
        BigDecimal averageSalary,

        @Schema(example = "60000.00")
        BigDecimal minSalary,

        @Schema(example = "210000.00")
        BigDecimal maxSalary,

        @Schema(description = "Reporting currency all figures are normalized to.", example = "USD")
        String currency
) {
}
