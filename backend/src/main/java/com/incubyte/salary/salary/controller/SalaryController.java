package com.incubyte.salary.salary.controller;

import com.incubyte.salary.salary.dto.SalaryRecordRequest;
import com.incubyte.salary.salary.dto.SalaryRecordResponse;
import com.incubyte.salary.salary.service.SalaryService;
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
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @GetMapping("/salaries")
    public List<SalaryRecordResponse> getHistory(@PathVariable Long employeeId) {
        return salaryService.getHistory(employeeId);
    }

    @GetMapping("/salary/current")
    public SalaryRecordResponse getCurrentSalary(@PathVariable Long employeeId) {
        return salaryService.getCurrentSalary(employeeId);
    }

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
    @PutMapping("/salaries/{salaryId}")
    public SalaryRecordResponse update(@PathVariable Long employeeId, @PathVariable Long salaryId,
                                        @Valid @RequestBody SalaryRecordRequest request) {
        return salaryService.updateSalaryRecord(salaryId, request);
    }
}
