package com.incubyte.salary.salary.dto;

import com.incubyte.salary.salary.entity.SalaryRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

public record SalaryRecordResponse(
        Long id,
        BigDecimal amount,
        String currency,
        LocalDate effectiveDate,
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
