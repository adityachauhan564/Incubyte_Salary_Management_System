package com.incubyte.salary.employee.repository;

import com.incubyte.salary.employee.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void savesAndReloadsAnEmployee() {
        Employee saved = employeeRepository.save(
                new Employee("Grace", "Hopper", "United States", "Engineering", "Rear Admiral"));

        Optional<Employee> found = employeeRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getLastName()).isEqualTo("Hopper");
    }
}
