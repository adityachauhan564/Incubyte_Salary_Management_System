package com.incubyte.salary.common.exception;

import java.time.LocalDate;

public class DuplicateSalaryRecordException extends RuntimeException {

    public DuplicateSalaryRecordException(Long employeeId, LocalDate effectiveDate) {
        super("A salary record already exists for employee " + employeeId + " effective on " + effectiveDate);
    }
}
