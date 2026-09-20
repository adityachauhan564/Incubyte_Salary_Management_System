package com.incubyte.salary.employee.controller;

import com.incubyte.salary.common.response.ApiError;
import com.incubyte.salary.employee.dto.EmployeeResponse;
import com.incubyte.salary.employee.dto.EmployeeSearchCriteria;
import com.incubyte.salary.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Read-only employee lookup and search - no create/update/delete (see docs/requirements.md non-goals).")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Get one employee by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "404", description = "No employee with this id",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @Operation(summary = "List employees, with optional search/filter and pagination",
            description = "All filters are optional and combine with AND. An empty result set is a normal "
                    + "200 response with zero content, not a 404. Supports standard Spring pagination params "
                    + "(page, size, sort) in addition to the filters listed below.")
    @ApiResponse(responseCode = "200", description = "A page of employees (possibly empty)")
    @GetMapping
    public PagedModel<EmployeeResponse> search(
            @Parameter(description = "Case-insensitive substring match against first or last name.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Exact match, e.g. \"United Kingdom\".")
            @RequestParam(required = false) String country,
            @Parameter(description = "Exact match, e.g. \"Engineering\".")
            @RequestParam(required = false) String department,
            @Parameter(description = "Exact match, e.g. \"Software Engineer\".")
            @RequestParam(required = false) String jobTitle,
            Pageable pageable) {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria(search, country, department, jobTitle);
        return new PagedModel<>(employeeService.search(criteria, pageable));
    }
}
