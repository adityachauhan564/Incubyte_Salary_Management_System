package com.incubyte.salary.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Consistent JSON error shape returned for every handled failure. */
@Schema(description = "Consistent error shape returned for every handled failure; never a stack trace.")
public record ApiError(

        @Schema(description = "When the error occurred.")
        Instant timestamp,

        @Schema(description = "HTTP status code.", example = "404")
        int status,

        @Schema(description = "Machine-readable error code, e.g. EMPLOYEE_NOT_FOUND, VALIDATION_ERROR, "
                + "DUPLICATE_SALARY_RECORD, UNSUPPORTED_CURRENCY, INTERNAL_ERROR.", example = "EMPLOYEE_NOT_FOUND")
        String error,

        @Schema(description = "Human-readable detail.")
        String message,

        @Schema(description = "The request path that produced this error.", example = "/api/employees/999")
        String path
) {
}
