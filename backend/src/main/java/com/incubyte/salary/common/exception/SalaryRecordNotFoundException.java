package com.incubyte.salary.common.exception;

public class SalaryRecordNotFoundException extends RuntimeException {

    private SalaryRecordNotFoundException(String message) {
        super(message);
    }

    public static SalaryRecordNotFoundException byId(Long salaryRecordId) {
        return new SalaryRecordNotFoundException("Salary record not found: " + salaryRecordId);
    }

    public static SalaryRecordNotFoundException noCurrentSalaryFor(Long employeeId) {
        return new SalaryRecordNotFoundException("No current salary found for employee: " + employeeId);
    }
}
