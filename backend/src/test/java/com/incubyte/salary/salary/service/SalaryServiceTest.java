package com.incubyte.salary.salary.service;

import com.incubyte.salary.common.exception.DuplicateSalaryRecordException;
import com.incubyte.salary.common.exception.EmployeeNotFoundException;
import com.incubyte.salary.common.exception.InvalidSalaryRecordException;
import com.incubyte.salary.common.exception.SalaryRecordNotFoundException;
import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.employee.repository.EmployeeRepository;
import com.incubyte.salary.salary.dto.SalaryRecordRequest;
import com.incubyte.salary.salary.dto.SalaryRecordResponse;
import com.incubyte.salary.salary.entity.SalaryRecord;
import com.incubyte.salary.salary.repository.SalaryRecordRepository;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDate.of(2026, 6, 1).atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);

    @Mock
    private SalaryRecordRepository salaryRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private SalaryService salaryService;
    private Employee employee;

    @BeforeEach
    void setUp() {
        salaryService = new SalaryService(
                salaryRecordRepository, employeeRepository,
                Validation.buildDefaultValidatorFactory().getValidator(), FIXED_CLOCK);

        employee = new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer");
        ReflectionTestUtils.setField(employee, "id", 1L);
    }

    @Test
    void createSalaryRecordSavesANewRecordForAnExistingEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        SalaryRecord saved = new SalaryRecord(employee, new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1));
        ReflectionTestUtils.setField(saved, "id", 10L);
        when(salaryRecordRepository.saveAndFlush(any(SalaryRecord.class))).thenReturn(saved);

        SalaryRecordResponse response = salaryService.createSalaryRecord(
                1L, new SalaryRecordRequest(new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1)));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.amount()).isEqualByComparingTo("95000.00");
    }

    @Test
    void createSalaryRecordThrowsWhenTheEmployeeDoesNotExist() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salaryService.createSalaryRecord(
                1L, new SalaryRecordRequest(new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1))))
                .isInstanceOf(EmployeeNotFoundException.class);

        verify(salaryRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void createSalaryRecordRejectsANonPositiveAmount() {
        assertThatThrownBy(() -> salaryService.createSalaryRecord(
                1L, new SalaryRecordRequest(BigDecimal.ZERO, "GBP", LocalDate.of(2026, 1, 1))))
                .isInstanceOf(InvalidSalaryRecordException.class);

        verify(employeeRepository, never()).findById(any());
        verify(salaryRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void createSalaryRecordThrowsDuplicateWhenTheDatabaseRejectsARepeatedEffectiveDate() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRecordRepository.saveAndFlush(any(SalaryRecord.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint violated"));

        assertThatThrownBy(() -> salaryService.createSalaryRecord(
                1L, new SalaryRecordRequest(new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1))))
                .isInstanceOf(DuplicateSalaryRecordException.class);
    }

    @Test
    void createSalaryRecordRejectsAnInvalidCurrencyCode() {
        assertThatThrownBy(() -> salaryService.createSalaryRecord(
                1L, new SalaryRecordRequest(new BigDecimal("95000.00"), "gbp", LocalDate.of(2026, 1, 1))))
                .isInstanceOf(InvalidSalaryRecordException.class);
    }

    @Test
    void createSalaryRecordRejectsAMissingEffectiveDate() {
        assertThatThrownBy(() -> salaryService.createSalaryRecord(
                1L, new SalaryRecordRequest(new BigDecimal("95000.00"), "GBP", null)))
                .isInstanceOf(InvalidSalaryRecordException.class);
    }

    @Test
    void updateSalaryRecordCorrectsAnExistingRecordInPlace() {
        SalaryRecord existing = new SalaryRecord(employee, new BigDecimal("90000.00"), "GBP", LocalDate.of(2026, 1, 1));
        ReflectionTestUtils.setField(existing, "id", 10L);
        when(salaryRecordRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(salaryRecordRepository.saveAndFlush(existing)).thenReturn(existing);

        SalaryRecordResponse response = salaryService.updateSalaryRecord(
                10L, new SalaryRecordRequest(new BigDecimal("92000.00"), "GBP", LocalDate.of(2026, 1, 2)));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.amount()).isEqualByComparingTo("92000.00");
        assertThat(response.effectiveDate()).isEqualTo(LocalDate.of(2026, 1, 2));
    }

    @Test
    void updateSalaryRecordThrowsWhenTheRecordDoesNotExist() {
        when(salaryRecordRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salaryService.updateSalaryRecord(
                404L, new SalaryRecordRequest(new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1))))
                .isInstanceOf(SalaryRecordNotFoundException.class);
    }

    @Test
    void updateSalaryRecordThrowsDuplicateWhenTheNewEffectiveDateCollidesWithAnotherRecord() {
        SalaryRecord existing = new SalaryRecord(employee, new BigDecimal("90000.00"), "GBP", LocalDate.of(2026, 1, 1));
        ReflectionTestUtils.setField(existing, "id", 10L);
        when(salaryRecordRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(salaryRecordRepository.saveAndFlush(existing))
                .thenThrow(new DataIntegrityViolationException("unique constraint violated"));

        assertThatThrownBy(() -> salaryService.updateSalaryRecord(
                10L, new SalaryRecordRequest(new BigDecimal("92000.00"), "GBP", LocalDate.of(2026, 1, 2))))
                .isInstanceOf(DuplicateSalaryRecordException.class);
    }

    @Test
    void getCurrentSalaryReturnsTheLatestRecordEffectiveAsOfTheClockDate() {
        SalaryRecord current = new SalaryRecord(employee, new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1));
        ReflectionTestUtils.setField(current, "id", 10L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRecordRepository.findFirstByEmployeeIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
                1L, LocalDate.of(2026, 6, 1))).thenReturn(Optional.of(current));

        SalaryRecordResponse response = salaryService.getCurrentSalary(1L);

        assertThat(response.amount()).isEqualByComparingTo("95000.00");
    }

    @Test
    void getCurrentSalaryThrowsWhenNoRecordIsEffectiveYet() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRecordRepository.findFirstByEmployeeIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
                any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salaryService.getCurrentSalary(1L))
                .isInstanceOf(SalaryRecordNotFoundException.class);
    }

    @Test
    void getHistoryReturnsRecordsOrderedMostRecentFirst() {
        SalaryRecord older = new SalaryRecord(employee, new BigDecimal("80000.00"), "GBP", LocalDate.of(2024, 1, 1));
        SalaryRecord newer = new SalaryRecord(employee, new BigDecimal("90000.00"), "GBP", LocalDate.of(2025, 1, 1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRecordRepository.findByEmployeeIdOrderByEffectiveDateDesc(1L)).thenReturn(List.of(newer, older));

        List<SalaryRecordResponse> history = salaryService.getHistory(1L);

        assertThat(history).extracting(SalaryRecordResponse::amount)
                .containsExactly(new BigDecimal("90000.00"), new BigDecimal("80000.00"));
    }

    @Test
    void getHistoryThrowsWhenTheEmployeeDoesNotExist() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salaryService.getHistory(1L))
                .isInstanceOf(EmployeeNotFoundException.class);
    }
}
