package com.incubyte.salary.salary.dto;

import com.incubyte.salary.salary.entity.SalaryRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "One salary record - either the current salary or one entry in the history list.")
public record SalaryRecordResponse(

        @Schema(description = "Generated primary key of this specific record.", example = "10")
        Long id,

        @Schema(description = "Always 2 decimal places.", example = "95000.00")
        BigDecimal amount,

        @Schema(description = "3-letter ISO 4217 currency code, in the record's native currency (not normalized).",
                example = "USD")
        String currency,

        @Schema(description = "Date this amount takes/took effect.", example = "2026-01-01")
        LocalDate effectiveDate,

        @Schema(description = "When this record was inserted; audit timestamp, not editable.")
        Instant createdAt
) {

    public static SalaryRecordResponse from(SalaryRecord record) {
        return new SalaryRecordResponse(
                record.getId(),
                // SQLite/Hibernate can return a whole-number amount with scale 0
                // (e.g. 95000 instead of 95000.00); force scale 2 so the API
                // always represents money consistently.
                record.getAmount().setScale(2, RoundingMode.HALF_UP),
                record.getCurrency(),
                record.getEffectiveDate(),
                record.getCreatedAt()
        );
    }
}
