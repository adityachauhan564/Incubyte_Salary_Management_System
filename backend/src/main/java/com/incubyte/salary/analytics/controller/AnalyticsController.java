package com.incubyte.salary.analytics.controller;

import com.incubyte.salary.analytics.dto.GroupedSalaryStats;
import com.incubyte.salary.analytics.dto.SalaryRangeBucket;
import com.incubyte.salary.analytics.dto.SalarySummary;
import com.incubyte.salary.analytics.service.AnalyticsService;
import com.incubyte.salary.common.response.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Analytics", description = "Compensation analytics over each employee's current salary, "
        + "normalized to USD via a static, documented exchange-rate table (docs/assumptions.md #7). "
        + "Never over full history - that would double-count raises.")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Org-wide compensation summary",
            description = "Headcount, total cost, average, median, min, and max - all normalized to USD.")
    @ApiResponse(responseCode = "200", description = "Summary (figures are null if no employee has a salary yet)")
    @GetMapping("/summary")
    public SalarySummary getSummary() {
        return analyticsService.getSummary();
    }

    @Operation(summary = "Compare salary cost and levels by department")
    @ApiResponse(responseCode = "200", description = "One entry per department that has at least one employee "
            + "with a current salary, sorted alphabetically")
    @GetMapping("/departments")
    public List<GroupedSalaryStats> getByDepartment() {
        return analyticsService.getByDepartment();
    }

    @Operation(summary = "Compare salary cost and levels by country")
    @ApiResponse(responseCode = "200", description = "One entry per country that has at least one employee "
            + "with a current salary, sorted alphabetically")
    @GetMapping("/countries")
    public List<GroupedSalaryStats> getByCountry() {
        return analyticsService.getByCountry();
    }

    @Operation(summary = "Salary distribution in fixed-width ranges",
            description = "Ranges start at 0 and step by bucketSize up to the highest occupied bucket; "
                    + "empty ranges in between are included with count 0 so the distribution has no gaps.")
    @ApiResponse(responseCode = "200", description = "Ordered list of buckets (empty list if no employee has a salary yet)")
    @ApiResponse(responseCode = "400", description = "bucketSize was not positive",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/distribution")
    public List<SalaryRangeBucket> getDistribution(
            @Parameter(description = "Width of each range, in USD.")
            @RequestParam(required = false, defaultValue = "25000") @Positive BigDecimal bucketSize) {
        return analyticsService.getDistribution(bucketSize);
    }
}
