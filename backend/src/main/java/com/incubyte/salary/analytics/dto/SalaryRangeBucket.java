package com.incubyte.salary.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** One fixed-width salary range, normalized to {@code currency}. rangeEnd is exclusive. */
@Schema(description = "One fixed-width salary range in the distribution. rangeEnd is exclusive.")
public record SalaryRangeBucket(

        @Schema(description = "Human-readable range, e.g. \"50000.00 - 75000.00\".")
        String label,

        @Schema(example = "50000.00")
        BigDecimal rangeStart,

        @Schema(description = "Exclusive upper bound.", example = "75000.00")
        BigDecimal rangeEnd,

        @Schema(description = "Employees whose current salary falls in this range. 0 for gap buckets.",
                example = "1450")
        long count,

        @Schema(description = "Reporting currency the range boundaries are expressed in.", example = "USD")
        String currency
) {
}
