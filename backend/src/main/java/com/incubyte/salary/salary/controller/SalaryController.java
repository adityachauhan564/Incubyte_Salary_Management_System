package com.incubyte.salary.salary.controller;

import com.incubyte.salary.common.response.ApiError;
import com.incubyte.salary.salary.dto.SalaryRecordRequest;
import com.incubyte.salary.salary.dto.SalaryRecordResponse;
import com.incubyte.salary.salary.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}")
@Tag(name = "Salaries", description = "Salary history is append-only: POST adds a new history entry, "
        + "PUT corrects an existing one in place. No delete (see docs/requirements.md non-goals).")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @Operation(summary = "Get an employee's full salary history",
            description = "Ordered most recent effective date first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History (possibly empty if the employee has no salary records)"),
            @ApiResponse(responseCode = "404", description = "No employee with this id",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/salaries")
    public List<SalaryRecordResponse> getHistory(@PathVariable Long employeeId) {
        return salaryService.getHistory(employeeId);
    }

    @Operation(summary = "Get an employee's current salary",
            description = "Derived, not stored: the history record with the latest effective date on or before "
                    + "today. Never a future-dated record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current salary"),
            @ApiResponse(responseCode = "404", description = "No employee with this id, or the employee has no "
                    + "salary record effective yet",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/salary/current")
    public SalaryRecordResponse getCurrentSalary(@PathVariable Long employeeId) {
        return salaryService.getCurrentSalary(employeeId);
    }

    @Operation(summary = "Create a new salary record (e.g. a raise)",
            description = "Adds a new point in the employee's history. Rejected if the employee already has a "
                    + "record on the same effectiveDate - correct that record with PUT instead of creating a "
                    + "second one for the same date.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Record created"),
            @ApiResponse(responseCode = "400", description = "Validation failure (non-positive amount, "
                    + "malformed currency code, or missing effectiveDate)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "No employee with this id",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "This employee already has a record on that "
                    + "effectiveDate",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/salaries")
    @ResponseStatus(HttpStatus.CREATED)
    public SalaryRecordResponse create(@PathVariable Long employeeId, @Valid @RequestBody SalaryRecordRequest request) {
        return salaryService.createSalaryRecord(employeeId, request);
    }

    /**
     * employeeId is accepted for URL consistency with the rest of the API,
     * but - like {@link SalaryService#updateSalaryRecord} - is not used to
     * verify the record actually belongs to that employee; see the
     * increment report for why that check is out of scope here.
     */
    @Operation(summary = "Correct an existing salary record in place",
            description = "Overwrites amount/currency/effectiveDate on the given record without changing its "
                    + "identity or creating a new history entry. Use POST instead if the intent is a new raise.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Record updated"),
            @ApiResponse(responseCode = "400", description = "Validation failure",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "No salary record with this id",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "The new effectiveDate collides with another "
                    + "existing record for this employee",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/salaries/{salaryId}")
    public SalaryRecordResponse update(
            @PathVariable Long employeeId,
            @Parameter(description = "Id of the specific salary record to correct.")
            @PathVariable Long salaryId,
            @Valid @RequestBody SalaryRecordRequest request) {
        return salaryService.updateSalaryRecord(salaryId, request);
    }
}
