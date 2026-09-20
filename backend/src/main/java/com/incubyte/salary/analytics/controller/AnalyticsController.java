package com.incubyte.salary.analytics.controller;

import com.incubyte.salary.analytics.dto.GroupedSalaryStats;
import com.incubyte.salary.analytics.dto.SalaryRangeBucket;
import com.incubyte.salary.analytics.dto.SalarySummary;
import com.incubyte.salary.analytics.service.AnalyticsService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public SalarySummary getSummary() {
        return analyticsService.getSummary();
    }

    @GetMapping("/departments")
    public List<GroupedSalaryStats> getByDepartment() {
        return analyticsService.getByDepartment();
    }

    @GetMapping("/countries")
    public List<GroupedSalaryStats> getByCountry() {
        return analyticsService.getByCountry();
    }

    @GetMapping("/distribution")
    public List<SalaryRangeBucket> getDistribution(
            @RequestParam(required = false, defaultValue = "25000") @Positive BigDecimal bucketSize) {
        return analyticsService.getDistribution(bucketSize);
    }
}
