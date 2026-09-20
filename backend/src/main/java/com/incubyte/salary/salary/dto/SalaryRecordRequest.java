package com.incubyte.salary.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request body for creating or updating a salary record.")
public record SalaryRecordRequest(

        @Schema(description = "Must be greater than 0.", example = "95000.00")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount,

        @Schema(description = "3-letter ISO 4217 currency code, upper case.", example = "USD")
        @NotNull
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO 4217 code")
        String currency,

        @Schema(description = "Date this amount takes effect. Must be unique per employee - "
                + "a second record for the same employee on the same date is rejected (409).",
                example = "2026-01-01")
        @NotNull
        LocalDate effectiveDate
) {
}
