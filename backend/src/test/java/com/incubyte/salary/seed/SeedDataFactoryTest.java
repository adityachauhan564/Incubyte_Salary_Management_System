package com.incubyte.salary.seed;

import com.incubyte.salary.salary.entity.SalaryRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SeedDataFactoryTest {

    private static final Set<String> VALID_CURRENCIES = Set.of("USD", "GBP", "EUR", "INR", "CAD", "AUD", "SGD", "BRL");

    private final SeedDataFactory factory = new SeedDataFactory();

    @Test
    void generatesTheRequestedNumberOfEmployees() {
        assertThat(factory.generate(200, 42L)).hasSize(200);
    }

    @Test
    void isDeterministicForTheSameSeed() {
        List<GeneratedEmployee> first = factory.generate(200, 42L);
        List<GeneratedEmployee> second = factory.generate(200, 42L);

        assertThat(describe(first)).isEqualTo(describe(second));
    }

    @Test
    void producesDifferentDataForADifferentSeed() {
        List<GeneratedEmployee> first = factory.generate(200, 42L);
        List<GeneratedEmployee> second = factory.generate(200, 7L);

        assertThat(describe(first)).isNotEqualTo(describe(second));
    }

    @Test
    void everyEmployeeHasAllRequiredFieldsPopulated() {
        for (GeneratedEmployee entry : factory.generate(200, 42L)) {
            var employee = entry.employee();
            assertThat(employee.getFirstName()).isNotBlank();
            assertThat(employee.getLastName()).isNotBlank();
            assertThat(employee.getCountry()).isNotBlank();
            assertThat(employee.getDepartment()).isNotBlank();
            assertThat(employee.getJobTitle()).isNotBlank();
        }
    }

    @Test
    void everyEmployeeHasAtLeastOneSalaryRecordEffectiveOnOrBeforeTheReferenceDate() {
        for (GeneratedEmployee entry : factory.generate(200, 42L)) {
            assertThat(entry.salaryRecords()).isNotEmpty();
            assertThat(entry.salaryRecords())
                    .anyMatch(record -> !record.getEffectiveDate().isAfter(SeedDataFactory.REFERENCE_DATE));
        }
    }

    @Test
    void everySalaryRecordHasAPositiveAmountAndAValidIsoCurrency() {
        for (GeneratedEmployee entry : factory.generate(200, 42L)) {
            for (SalaryRecord record : entry.salaryRecords()) {
                assertThat(record.getAmount()).isGreaterThan(BigDecimal.ZERO);
                assertThat(record.getCurrency()).isIn(VALID_CURRENCIES);
            }
        }
    }

    @Test
    void aMeaningfulSubsetOfEmployeesHaveMultipleSalaryRecords() {
        List<GeneratedEmployee> generated = factory.generate(2000, 42L);
        long withHistory = generated.stream().filter(entry -> entry.salaryRecords().size() > 1).count();

        assertThat((double) withHistory / generated.size()).isBetween(0.1, 0.5);
    }

    @Test
    void salaryRecordsForTheSameEmployeeHaveDistinctEffectiveDates() {
        for (GeneratedEmployee entry : factory.generate(200, 42L)) {
            List<LocalDate> dates = entry.salaryRecords().stream().map(SalaryRecord::getEffectiveDate).toList();
            assertThat(dates).doesNotHaveDuplicates();
        }
    }

    private List<String> describe(List<GeneratedEmployee> employees) {
        return employees.stream()
                .map(entry -> entry.employee().getFirstName() + "|" + entry.employee().getLastName() + "|"
                        + entry.employee().getCountry() + "|" + entry.employee().getJobTitle() + "|"
                        + entry.salaryRecords().size())
                .toList();
    }
}
