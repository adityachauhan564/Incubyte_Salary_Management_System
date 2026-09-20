package com.incubyte.salary.analytics.service;

import com.incubyte.salary.analytics.dto.GroupedSalaryStats;
import com.incubyte.salary.analytics.dto.SalaryRangeBucket;
import com.incubyte.salary.analytics.dto.SalarySummary;
import com.incubyte.salary.common.exception.UnsupportedCurrencyException;
import com.incubyte.salary.employee.repository.EmployeeRepository;
import com.incubyte.salary.salary.repository.CurrentSalarySnapshot;
import com.incubyte.salary.salary.repository.SalaryRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDate.of(2026, 6, 1).atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRecordRepository salaryRecordRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                employeeRepository, salaryRecordRepository, new ExchangeRateConfig(), FIXED_CLOCK);
    }

    @Test
    void summaryComputesHeadcountCostAverageMedianMinMaxNormalizedAcrossCurrencies() {
        when(employeeRepository.count()).thenReturn(5L);
        when(salaryRecordRepository.findCurrentSalarySnapshots(LocalDate.of(2026, 6, 1))).thenReturn(List.of(
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("100000.00"), "USD"),
                new CurrentSalarySnapshot("United Kingdom", "Engineering", new BigDecimal("80000.00"), "GBP"),
                new CurrentSalarySnapshot("India", "Engineering", new BigDecimal("830000.00"), "INR")
        ));

        SalarySummary summary = analyticsService.getSummary();

        assertThat(summary.totalEmployees()).isEqualTo(5L);
        assertThat(summary.currency()).isEqualTo("USD");
        assertThat(summary.totalSalaryCost()).isEqualByComparingTo("210000.00");
        assertThat(summary.averageSalary()).isEqualByComparingTo("70000.00");
        assertThat(summary.medianSalary()).isEqualByComparingTo("100000.00");
        assertThat(summary.minSalary()).isEqualByComparingTo("10000.00");
        assertThat(summary.maxSalary()).isEqualByComparingTo("100000.00");
    }

    @Test
    void summaryUsesAverageOfTheTwoMiddleValuesForAnEvenNumberOfEmployees() {
        when(employeeRepository.count()).thenReturn(4L);
        when(salaryRecordRepository.findCurrentSalarySnapshots(any())).thenReturn(List.of(
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("10000.00"), "USD"),
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("20000.00"), "USD"),
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("30000.00"), "USD"),
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("40000.00"), "USD")
        ));

        SalarySummary summary = analyticsService.getSummary();

        assertThat(summary.medianSalary()).isEqualByComparingTo("25000.00");
    }

    @Test
    void summaryReturnsZeroCostAndNullStatsWhenNoEmployeeHasACurrentSalary() {
        when(employeeRepository.count()).thenReturn(5L);
        when(salaryRecordRepository.findCurrentSalarySnapshots(any())).thenReturn(List.of());

        SalarySummary summary = analyticsService.getSummary();

        assertThat(summary.totalEmployees()).isEqualTo(5L);
        assertThat(summary.totalSalaryCost()).isEqualByComparingTo("0.00");
        assertThat(summary.averageSalary()).isNull();
        assertThat(summary.medianSalary()).isNull();
        assertThat(summary.minSalary()).isNull();
        assertThat(summary.maxSalary()).isNull();
    }

    @Test
    void summaryThrowsForAnUnconfiguredCurrency() {
        when(salaryRecordRepository.findCurrentSalarySnapshots(any())).thenReturn(List.of(
                new CurrentSalarySnapshot("Japan", "Engineering", new BigDecimal("5000000.00"), "JPY")
        ));

        assertThatThrownBy(() -> analyticsService.getSummary())
                .isInstanceOf(UnsupportedCurrencyException.class);
    }

    @Test
    void byDepartmentGroupsAndComputesStatsPerDepartmentSortedAlphabetically() {
        when(salaryRecordRepository.findCurrentSalarySnapshots(any())).thenReturn(List.of(
                new CurrentSalarySnapshot("United States", "Sales", new BigDecimal("60000.00"), "USD"),
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("100000.00"), "USD"),
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("120000.00"), "USD")
        ));

        List<GroupedSalaryStats> byDepartment = analyticsService.getByDepartment();

        assertThat(byDepartment).extracting(GroupedSalaryStats::name).containsExactly("Engineering", "Sales");
        GroupedSalaryStats engineering = byDepartment.get(0);
        assertThat(engineering.headcount()).isEqualTo(2);
        assertThat(engineering.totalCost()).isEqualByComparingTo("220000.00");
        assertThat(engineering.averageSalary()).isEqualByComparingTo("110000.00");
        assertThat(engineering.minSalary()).isEqualByComparingTo("100000.00");
        assertThat(engineering.maxSalary()).isEqualByComparingTo("120000.00");
    }

    @Test
    void byCountryGroupsByCountryRatherThanDepartment() {
        when(salaryRecordRepository.findCurrentSalarySnapshots(any())).thenReturn(List.of(
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("100000.00"), "USD"),
                new CurrentSalarySnapshot("United Kingdom", "Engineering", new BigDecimal("80000.00"), "GBP")
        ));

        List<GroupedSalaryStats> byCountry = analyticsService.getByCountry();

        assertThat(byCountry).extracting(GroupedSalaryStats::name)
                .containsExactly("United Kingdom", "United States");
        assertThat(byCountry.get(0).totalCost()).isEqualByComparingTo("100000.00"); // 80000 GBP normalized
    }

    @Test
    void distributionBucketsAmountsAndFillsEmptyGapsWithZero() {
        when(salaryRecordRepository.findCurrentSalarySnapshots(any())).thenReturn(List.of(
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("10000.00"), "USD"),
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("15000.00"), "USD"),
                new CurrentSalarySnapshot("United States", "Engineering", new BigDecimal("55000.00"), "USD")
        ));

        List<SalaryRangeBucket> distribution = analyticsService.getDistribution(new BigDecimal("25000"));

        assertThat(distribution).hasSize(3);
        assertThat(distribution.get(0).count()).isEqualTo(2); // 0-25000: the two 10k/15k salaries
        assertThat(distribution.get(1).count()).isEqualTo(0); // 25000-50000: empty gap
        assertThat(distribution.get(2).count()).isEqualTo(1); // 50000-75000: the 55k salary
        assertThat(distribution.get(2).rangeStart()).isEqualByComparingTo("50000.00");
        assertThat(distribution.get(2).rangeEnd()).isEqualByComparingTo("75000.00");
    }

    @Test
    void distributionRejectsANonPositiveBucketSize() {
        assertThatThrownBy(() -> analyticsService.getDistribution(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void distributionReturnsAnEmptyListWhenNoEmployeeHasACurrentSalary() {
        when(salaryRecordRepository.findCurrentSalarySnapshots(any())).thenReturn(List.of());

        assertThat(analyticsService.getDistribution(new BigDecimal("25000"))).isEmpty();
    }
}
