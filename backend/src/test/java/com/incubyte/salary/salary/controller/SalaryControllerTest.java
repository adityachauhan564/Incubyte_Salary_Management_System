package com.incubyte.salary.salary.controller;

import com.incubyte.salary.common.exception.EmployeeNotFoundException;
import com.incubyte.salary.common.exception.SalaryRecordNotFoundException;
import com.incubyte.salary.salary.dto.SalaryRecordResponse;
import com.incubyte.salary.salary.service.SalaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SalaryController.class)
class SalaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalaryService salaryService;

    @Test
    void getHistoryReturnsTheEmployeesSalaryRecords() throws Exception {
        when(salaryService.getHistory(1L)).thenReturn(List.of(
                new SalaryRecordResponse(2L, new BigDecimal("90000.00"), "GBP", LocalDate.of(2025, 1, 1), Instant.now()),
                new SalaryRecordResponse(1L, new BigDecimal("80000.00"), "GBP", LocalDate.of(2024, 1, 1), Instant.now())
        ));

        mockMvc.perform(get("/api/employees/1/salaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amount").value(90000.00));
    }

    @Test
    void getCurrentSalaryReturnsTheLatestEffectiveRecord() throws Exception {
        when(salaryService.getCurrentSalary(1L)).thenReturn(
                new SalaryRecordResponse(2L, new BigDecimal("90000.00"), "GBP", LocalDate.of(2025, 1, 1), Instant.now()));

        mockMvc.perform(get("/api/employees/1/salary/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(90000.00));
    }

    @Test
    void getCurrentSalaryReturns404WhenNoneIsEffectiveYet() throws Exception {
        when(salaryService.getCurrentSalary(1L)).thenThrow(SalaryRecordNotFoundException.noCurrentSalaryFor(1L));

        mockMvc.perform(get("/api/employees/1/salary/current"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("SALARY_RECORD_NOT_FOUND"));
    }

    @Test
    void createReturns201WithTheCreatedRecord() throws Exception {
        when(salaryService.createSalaryRecord(eq(1L), any())).thenReturn(
                new SalaryRecordResponse(10L, new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1), Instant.now()));

        mockMvc.perform(post("/api/employees/1/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":95000.00,"currency":"GBP","effectiveDate":"2026-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.amount").value(95000.00));
    }

    @Test
    void createReturns400ForAnInvalidRequestWithoutCallingTheService() throws Exception {
        mockMvc.perform(post("/api/employees/1/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":0,"currency":"gbp","effectiveDate":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(salaryService, never()).createSalaryRecord(any(), any());
    }

    @Test
    void createReturns404WhenTheEmployeeDoesNotExist() throws Exception {
        when(salaryService.createSalaryRecord(eq(1L), any())).thenThrow(new EmployeeNotFoundException(1L));

        mockMvc.perform(post("/api/employees/1/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":95000.00,"currency":"GBP","effectiveDate":"2026-01-01"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("EMPLOYEE_NOT_FOUND"));
    }

    @Test
    void updateReturns200WithTheCorrectedRecord() throws Exception {
        when(salaryService.updateSalaryRecord(eq(10L), any())).thenReturn(
                new SalaryRecordResponse(10L, new BigDecimal("92000.00"), "GBP", LocalDate.of(2026, 1, 2), Instant.now()));

        mockMvc.perform(put("/api/employees/1/salaries/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":92000.00,"currency":"GBP","effectiveDate":"2026-01-02"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(92000.00))
                .andExpect(jsonPath("$.effectiveDate").value("2026-01-02"));
    }

    @Test
    void updateReturns404WhenTheSalaryRecordDoesNotExist() throws Exception {
        when(salaryService.updateSalaryRecord(eq(404L), any()))
                .thenThrow(SalaryRecordNotFoundException.byId(404L));

        mockMvc.perform(put("/api/employees/1/salaries/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":92000.00,"currency":"GBP","effectiveDate":"2026-01-02"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("SALARY_RECORD_NOT_FOUND"));
    }
}
