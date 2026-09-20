package com.incubyte.salary.salary.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Input for creating or updating a salary record. Creating one adds a new
 * point in the employee's salary history (e.g. a raise); updating one
 * corrects the fields of an existing record without changing its identity.
 */
public record SalaryRecordRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount,

        @NotNull
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO 4217 code")
        String currency,

        @NotNull
        LocalDate effectiveDate
) {
}
