package com.incubyte.salary.employee.dto;

import com.incubyte.salary.employee.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employee summary/detail used by both the list and the single-employee endpoints.")
public record EmployeeResponse(

        @Schema(description = "Generated primary key.", example = "1")
        Long id,

        @Schema(example = "Ada")
        String firstName,

        @Schema(example = "Lovelace")
        String lastName,

        @Schema(description = "Country of employment.", example = "United Kingdom")
        String country,

        @Schema(example = "Engineering")
        String department,

        @Schema(example = "Software Engineer")
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
