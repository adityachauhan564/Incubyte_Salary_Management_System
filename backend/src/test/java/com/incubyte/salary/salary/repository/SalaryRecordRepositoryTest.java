package com.incubyte.salary.salary.repository;

import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.employee.repository.EmployeeRepository;
import com.incubyte.salary.salary.entity.SalaryRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SalaryRecordRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = employeeRepository.save(
                new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer"));
    }

    @Test
    void findsTheLatestRecordEffectiveOnOrBeforeTheGivenDate() {
        salaryRecordRepository.save(
                new SalaryRecord(employee, new BigDecimal("80000.00"), "GBP", LocalDate.of(2024, 1, 1)));
        SalaryRecord raise = salaryRecordRepository.save(
                new SalaryRecord(employee, new BigDecimal("90000.00"), "GBP", LocalDate.of(2025, 1, 1)));
        salaryRecordRepository.save(
                new SalaryRecord(employee, new BigDecimal("100000.00"), "GBP", LocalDate.of(2027, 1, 1)));

        Optional<SalaryRecord> current = salaryRecordRepository
                .findFirstByEmployeeIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
                        employee.getId(), LocalDate.of(2026, 6, 1));

        assertThat(current).isPresent();
        assertThat(current.get().getId()).isEqualTo(raise.getId());
        assertThat(current.get().getAmount()).isEqualByComparingTo("90000.00");
    }

    @Test
    void returnsEmptyWhenNoRecordIsEffectiveYet() {
        salaryRecordRepository.save(
                new SalaryRecord(employee, new BigDecimal("100000.00"), "GBP", LocalDate.of(2027, 1, 1)));

        Optional<SalaryRecord> current = salaryRecordRepository
                .findFirstByEmployeeIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
                        employee.getId(), LocalDate.of(2026, 6, 1));

        assertThat(current).isEmpty();
    }

    @Test
    void findCurrentSalarySnapshotsReturnsExactlyOneRowPerEmployee() {
        Employee otherEmployee = employeeRepository.save(
                new Employee("Grace", "Hopper", "United States", "Engineering", "Rear Admiral"));
        salaryRecordRepository.save(
                new SalaryRecord(employee, new BigDecimal("80000.00"), "GBP", LocalDate.of(2025, 1, 1)));
        SalaryRecord latestRecord = salaryRecordRepository.save(
                new SalaryRecord(employee, new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1)));
        salaryRecordRepository.save(
                new SalaryRecord(otherEmployee, new BigDecimal("100000.00"), "USD", LocalDate.of(2025, 1, 1)));

        List<CurrentSalarySnapshot> snapshots =
                salaryRecordRepository.findCurrentSalarySnapshots(LocalDate.of(2026, 6, 1));

        assertThat(snapshots).hasSize(2);
        assertThat(snapshots)
                .anySatisfy(s -> assertThat(s.amount()).isEqualByComparingTo("100000.00"))
                .anySatisfy(s -> assertThat(s.amount()).isEqualByComparingTo(latestRecord.getAmount()));
    }

    @Test
    void rejectsASecondSalaryRecordForTheSameEmployeeOnTheSameEffectiveDate() {
        salaryRecordRepository.saveAndFlush(
                new SalaryRecord(employee, new BigDecimal("90000.00"), "GBP", LocalDate.of(2026, 1, 1)));

        // Not necessarily DataIntegrityViolationException specifically: this SQLite dialect
        // doesn't classify the violation into that subtype, only into the broader DataAccessException.
        assertThatThrownBy(() -> salaryRecordRepository.saveAndFlush(
                new SalaryRecord(employee, new BigDecimal("95000.00"), "GBP", LocalDate.of(2026, 1, 1))))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("UNIQUE constraint failed");
    }

    @Test
    void returnsHistoryOrderedMostRecentFirst() {
        SalaryRecord oldest = salaryRecordRepository.save(
                new SalaryRecord(employee, new BigDecimal("80000.00"), "GBP", LocalDate.of(2024, 1, 1)));
        SalaryRecord newest = salaryRecordRepository.save(
                new SalaryRecord(employee, new BigDecimal("90000.00"), "GBP", LocalDate.of(2025, 1, 1)));

        List<SalaryRecord> history = salaryRecordRepository.findByEmployeeIdOrderByEffectiveDateDesc(employee.getId());

        assertThat(history)
                .extracting(SalaryRecord::getId)
                .containsExactly(newest.getId(), oldest.getId());
    }
}
