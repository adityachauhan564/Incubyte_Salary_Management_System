package com.incubyte.salary.employee.dto;

/**
 * Optional filters for listing employees. Any field left null/blank is not
 * applied (e.g. {@code new EmployeeSearchCriteria(null, "France", null, null)}
 * lists everyone in France regardless of name/department/job title).
 */
public record EmployeeSearchCriteria(String search, String country, String department, String jobTitle) {

    public static EmployeeSearchCriteria empty() {
        return new EmployeeSearchCriteria(null, null, null, null);
    }
}
