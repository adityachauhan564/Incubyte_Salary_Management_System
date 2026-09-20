package com.incubyte.salary;

import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.employee.repository.EmployeeRepository;
import com.incubyte.salary.salary.dto.SalaryRecordRequest;
import com.incubyte.salary.salary.entity.SalaryRecord;
import com.incubyte.salary.salary.repository.SalaryRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real stack - controller, service, repository, SQLite - for
 * the flows most worth verifying end-to-end: JSON (de)serialization of
 * BigDecimal/LocalDate through Jackson, real Bean Validation wiring via
 * {@code @Valid}, and that a created salary record is immediately visible
 * through the read endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SalaryManagementApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = employeeRepository.save(
                new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer"));
        salaryRecordRepository.save(
                new SalaryRecord(employee, new BigDecimal("90000.00"), "GBP", LocalDate.of(2024, 1, 1)));
    }

    @Test
    void fetchesAnEmployeeById() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.country").value("United Kingdom"));
    }

    @Test
    void listsAndFiltersEmployeesWithPagination() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .param("country", "United Kingdom")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].country").value("United Kingdom"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void returns404ForAnUnknownEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EMPLOYEE_NOT_FOUND"));
    }

    @Test
    void createsASalaryRecordAndItIsImmediatelyVisibleAsCurrentAndInHistory() throws Exception {
        SalaryRecordRequest request = new SalaryRecordRequest(
                new BigDecimal("95000.00"), "GBP", LocalDate.of(2024, 6, 1));

        mockMvc.perform(post("/api/employees/{id}/salaries", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(95000.00))
                .andExpect(jsonPath("$.currency").value("GBP"));

        mockMvc.perform(get("/api/employees/{id}/salaries", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/employees/{id}/salary/current", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(95000.00));
    }

    @Test
    void rejectsADuplicateSalaryRecordWithAClearConflictErrorRatherThanAServerError() throws Exception {
        // setUp() already created a record for this employee effective 2024-01-01.
        SalaryRecordRequest duplicate = new SalaryRecordRequest(
                new BigDecimal("95000.00"), "GBP", LocalDate.of(2024, 1, 1));

        mockMvc.perform(post("/api/employees/{id}/salaries", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_SALARY_RECORD"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rejectsAnInvalidSalaryCreationRequestWithAConsistentErrorBody() throws Exception {
        mockMvc.perform(post("/api/employees/{id}/salaries", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":0,"currency":"gbp","effectiveDate":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
