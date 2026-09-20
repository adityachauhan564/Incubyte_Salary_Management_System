package com.incubyte.salary.employee.service;

import com.incubyte.salary.common.exception.EmployeeNotFoundException;
import com.incubyte.salary.employee.dto.EmployeeResponse;
import com.incubyte.salary.employee.dto.EmployeeSearchCriteria;
import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository);
    }

    @Test
    void getByIdReturnsAMappedResponseWhenTheEmployeeExists() {
        Employee employee = new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer");
        ReflectionTestUtils.setField(employee, "id", 1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponse response = employeeService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.lastName()).isEqualTo("Lovelace");
    }

    @Test
    void getByIdThrowsWhenTheEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(99L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchReturnsAMappedPageUsingTheGivenCriteriaAndPageable() {
        Employee employee = new Employee("Grace", "Hopper", "United States", "Engineering", "Rear Admiral");
        ReflectionTestUtils.setField(employee, "id", 2L);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);
        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<EmployeeResponse> result = employeeService.search(
                new EmployeeSearchCriteria(null, "United States", null, null), pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).lastName()).isEqualTo("Hopper");
    }
}
