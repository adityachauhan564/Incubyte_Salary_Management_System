package com.incubyte.salary.employee.repository;

import com.incubyte.salary.employee.dto.EmployeeSearchCriteria;
import com.incubyte.salary.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeSpecificationsTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.saveAll(List.of(
                new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer"),
                new Employee("Grace", "Hopper", "United States", "Engineering", "Rear Admiral"),
                new Employee("Alan", "Turing", "United Kingdom", "Research", "Mathematician")
        ));
    }

    @Test
    void filtersByCountry() {
        Page<Employee> page = employeeRepository.findAll(
                EmployeeSpecifications.matching(new EmployeeSearchCriteria(null, "United Kingdom", null, null)),
                PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Employee::getLastName)
                .containsExactlyInAnyOrder("Lovelace", "Turing");
    }

    @Test
    void filtersByCaseInsensitiveNameSearch() {
        Page<Employee> page = employeeRepository.findAll(
                EmployeeSpecifications.matching(new EmployeeSearchCriteria("hopper", null, null, null)),
                PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Employee::getLastName).containsExactly("Hopper");
    }

    @Test
    void combinesMultipleFilters() {
        Page<Employee> page = employeeRepository.findAll(
                EmployeeSpecifications.matching(new EmployeeSearchCriteria(null, "United Kingdom", "Research", null)),
                PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Employee::getLastName).containsExactly("Turing");
    }

    @Test
    void returnsEveryoneWhenNoCriteriaAreGiven() {
        Page<Employee> page = employeeRepository.findAll(
                EmployeeSpecifications.matching(EmployeeSearchCriteria.empty()),
                PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
    }
}
