package com.incubyte.salary.employee.controller;

import com.incubyte.salary.employee.dto.EmployeeResponse;
import com.incubyte.salary.employee.dto.EmployeeSearchCriteria;
import com.incubyte.salary.employee.service.EmployeeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @GetMapping
    public PagedModel<EmployeeResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String jobTitle,
            Pageable pageable) {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria(search, country, department, jobTitle);
        return new PagedModel<>(employeeService.search(criteria, pageable));
    }
}
