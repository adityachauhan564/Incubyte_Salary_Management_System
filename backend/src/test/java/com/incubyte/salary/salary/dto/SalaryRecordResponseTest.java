package com.incubyte.salary.salary.dto;

import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.salary.entity.SalaryRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryRecordResponseTest {

    @Test
    void mapsAllFieldsFromTheEntity() {
        Employee employee = new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer");
        SalaryRecord record = new SalaryRecord(employee, new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1));

        SalaryRecordResponse response = SalaryRecordResponse.from(record);

        assertThat(response.id()).isEqualTo(record.getId());
        assertThat(response.amount()).isEqualByComparingTo("95000.00");
        assertThat(response.currency()).isEqualTo("GBP");
        assertThat(response.effectiveDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.createdAt()).isEqualTo(record.getCreatedAt());
    }

    @Test
    void alwaysFormatsTheAmountWithTwoDecimalPlacesRegardlessOfStoredScale() {
        Employee employee = new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer");
        SalaryRecord record = new SalaryRecord(employee, new BigDecimal("95000"), "GBP", LocalDate.of(2026, 1, 1));

        SalaryRecordResponse response = SalaryRecordResponse.from(record);

        assertThat(response.amount().scale()).isEqualTo(2);
        assertThat(response.amount().toPlainString()).isEqualTo("95000.00");
    }
}
