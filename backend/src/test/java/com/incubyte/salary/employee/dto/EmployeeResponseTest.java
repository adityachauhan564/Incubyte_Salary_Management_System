package com.incubyte.salary.employee.dto;

import com.incubyte.salary.employee.entity.Employee;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeResponseTest {

    @Test
    void mapsAllFieldsFromTheEntity() {
        Employee employee = new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer");

        EmployeeResponse response = EmployeeResponse.from(employee);

        assertThat(response.id()).isEqualTo(employee.getId());
        assertThat(response.firstName()).isEqualTo("Ada");
        assertThat(response.lastName()).isEqualTo("Lovelace");
        assertThat(response.country()).isEqualTo("United Kingdom");
        assertThat(response.department()).isEqualTo("Engineering");
        assertThat(response.jobTitle()).isEqualTo("Software Engineer");
    }
}
