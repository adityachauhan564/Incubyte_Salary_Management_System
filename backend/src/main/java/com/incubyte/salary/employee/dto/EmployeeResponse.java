package com.incubyte.salary.employee.dto;

import com.incubyte.salary.employee.entity.Employee;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String country,
        String department,
        String jobTitle
) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getCountry(),
                employee.getDepartment(),
                employee.getJobTitle()
        );
    }
}
