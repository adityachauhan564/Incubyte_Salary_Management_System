package com.incubyte.salary.analytics.controller;

import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.employee.repository.EmployeeRepository;
import com.incubyte.salary.salary.entity.SalaryRecord;
import com.incubyte.salary.salary.repository.SalaryRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Exercises the analytics endpoints against real SQLite, including cross-currency normalization. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnalyticsApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    @BeforeEach
    void setUp() {
        Employee usEmployee = employeeRepository.save(
                new Employee("Grace", "Hopper", "United States", "Engineering", "Rear Admiral"));
        Employee ukEmployee = employeeRepository.save(
                new Employee("Ada", "Lovelace", "United Kingdom", "Sales", "Account Executive"));

        salaryRecordRepository.save(
                new SalaryRecord(usEmployee, new BigDecimal("100000.00"), "USD", LocalDate.of(2025, 1, 1)));
        salaryRecordRepository.save(
                new SalaryRecord(ukEmployee, new BigDecimal("80000.00"), "GBP", LocalDate.of(2025, 1, 1)));
    }

    @Test
    void summaryReflectsAllEmployeesNormalizedToUsd() throws Exception {
        mockMvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.totalEmployees").value(2))
                .andExpect(jsonPath("$.totalSalaryCost").value(200000.00))
                .andExpect(jsonPath("$.averageSalary").value(100000.00));
    }

    @Test
    void departmentsAreCompared() throws Exception {
        mockMvc.perform(get("/api/analytics/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Engineering"))
                .andExpect(jsonPath("$[0].totalCost").value(100000.00));
    }

    @Test
    void countriesAreComparedWithCurrencyNormalizedToUsd() throws Exception {
        mockMvc.perform(get("/api/analytics/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='United Kingdom')].totalCost").value(100000.00));
    }

    @Test
    void distributionBucketsBothEmployeesIntoTheSameRange() throws Exception {
        mockMvc.perform(get("/api/analytics/distribution").param("bucketSize", "50000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.rangeStart==100000.00)].count").value(2));
    }

    @Test
    void distributionRejectsANonPositiveBucketSize() throws Exception {
        mockMvc.perform(get("/api/analytics/distribution").param("bucketSize", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
