package com.incubyte.salary.analytics.service;

import com.incubyte.salary.analytics.dto.GroupedSalaryStats;
import com.incubyte.salary.analytics.dto.SalaryRangeBucket;
import com.incubyte.salary.analytics.dto.SalarySummary;
import com.incubyte.salary.employee.repository.EmployeeRepository;
import com.incubyte.salary.salary.repository.CurrentSalarySnapshot;
import com.incubyte.salary.salary.repository.SalaryRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Computes compensation analytics over each employee's current salary
 * (never over full history - that would double-count raises). The
 * expensive relational work (finding each employee's current record) is
 * pushed to the database via {@link SalaryRecordRepository#findCurrentSalarySnapshots};
 * currency normalization and the final numeric aggregation (including
 * median, which SQLite has no native function for) run in Java over that
 * already-reduced, ~one-row-per-employee result.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final EmployeeRepository employeeRepository;
    private final SalaryRecordRepository salaryRecordRepository;
    private final ExchangeRateConfig exchangeRateConfig;
    private final Clock clock;

    public AnalyticsService(EmployeeRepository employeeRepository,
                             SalaryRecordRepository salaryRecordRepository,
                             ExchangeRateConfig exchangeRateConfig,
                             Clock clock) {
        this.employeeRepository = employeeRepository;
        this.salaryRecordRepository = salaryRecordRepository;
        this.exchangeRateConfig = exchangeRateConfig;
        this.clock = clock;
    }

    public SalarySummary getSummary() {
        List<BigDecimal> normalized = normalizedCurrentSalaries();

        return new SalarySummary(
                employeeRepository.count(),
                sum(normalized),
                average(normalized),
                median(normalized),
                min(normalized),
                max(normalized),
                ExchangeRateConfig.REPORTING_CURRENCY
        );
    }

    public List<GroupedSalaryStats> getByDepartment() {
        return groupBy(CurrentSalarySnapshot::department);
    }

    public List<GroupedSalaryStats> getByCountry() {
        return groupBy(CurrentSalarySnapshot::country);
    }

    public List<SalaryRangeBucket> getDistribution(BigDecimal bucketSize) {
        if (bucketSize.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("bucketSize must be positive");
        }

        List<BigDecimal> normalized = normalizedCurrentSalaries();
        if (normalized.isEmpty()) {
            return List.of();
        }

        Map<Integer, Long> countsByBucketIndex = normalized.stream()
                .collect(Collectors.groupingBy(amount -> bucketIndex(amount, bucketSize), Collectors.counting()));
        int maxIndex = countsByBucketIndex.keySet().stream().max(Integer::compareTo).orElse(0);

        List<SalaryRangeBucket> buckets = new ArrayList<>(maxIndex + 1);
        for (int i = 0; i <= maxIndex; i++) {
            BigDecimal start = bucketSize.multiply(BigDecimal.valueOf(i)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal end = bucketSize.multiply(BigDecimal.valueOf(i + 1L)).setScale(2, RoundingMode.HALF_UP);
            long count = countsByBucketIndex.getOrDefault(i, 0L);
            buckets.add(new SalaryRangeBucket(
                    start + " - " + end, start, end, count, ExchangeRateConfig.REPORTING_CURRENCY));
        }
        return buckets;
    }

    private List<GroupedSalaryStats> groupBy(Function<CurrentSalarySnapshot, String> groupKey) {
        Map<String, List<BigDecimal>> byGroup = new LinkedHashMap<>();
        for (CurrentSalarySnapshot snapshot : currentSnapshots()) {
            byGroup.computeIfAbsent(groupKey.apply(snapshot), key -> new ArrayList<>())
                    .add(exchangeRateConfig.toReportingCurrency(snapshot.amount(), snapshot.currency()));
        }

        return byGroup.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new GroupedSalaryStats(
                        entry.getKey(),
                        entry.getValue().size(),
                        sum(entry.getValue()),
                        average(entry.getValue()),
                        min(entry.getValue()),
                        max(entry.getValue()),
                        ExchangeRateConfig.REPORTING_CURRENCY))
                .toList();
    }

    private List<CurrentSalarySnapshot> currentSnapshots() {
        return salaryRecordRepository.findCurrentSalarySnapshots(LocalDate.now(clock));
    }

    private List<BigDecimal> normalizedCurrentSalaries() {
        return currentSnapshots().stream()
                .map(snapshot -> exchangeRateConfig.toReportingCurrency(snapshot.amount(), snapshot.currency()))
                .toList();
    }

    private int bucketIndex(BigDecimal amount, BigDecimal bucketSize) {
        return amount.divide(bucketSize, 0, RoundingMode.DOWN).intValue();
    }

    private BigDecimal sum(List<BigDecimal> amounts) {
        return amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal average(List<BigDecimal> amounts) {
        if (amounts.isEmpty()) {
            return null;
        }
        return sum(amounts).divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal median(List<BigDecimal> amounts) {
        if (amounts.isEmpty()) {
            return null;
        }
        List<BigDecimal> sorted = amounts.stream().sorted().toList();
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(middle);
        }
        return sorted.get(middle - 1).add(sorted.get(middle)).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal min(List<BigDecimal> amounts) {
        return amounts.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    private BigDecimal max(List<BigDecimal> amounts) {
        return amounts.stream().max(Comparator.naturalOrder()).orElse(null);
    }
}
