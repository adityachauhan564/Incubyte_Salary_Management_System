package com.incubyte.salary.seed;

import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.salary.entity.SalaryRecord;

import java.util.List;

/** One generated employee bundled with its (not-yet-persisted) salary history. */
public record GeneratedEmployee(Employee employee, List<SalaryRecord> salaryRecords) {
}
