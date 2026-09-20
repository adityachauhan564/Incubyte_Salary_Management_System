package com.incubyte.salary.employee.service;

import com.incubyte.salary.common.exception.EmployeeNotFoundException;
import com.incubyte.salary.employee.dto.EmployeeResponse;
import com.incubyte.salary.employee.dto.EmployeeSearchCriteria;
import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.employee.repository.EmployeeRepository;
import com.incubyte.salary.employee.repository.EmployeeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public EmployeeResponse getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return EmployeeResponse.from(employee);
    }

    public Page<EmployeeResponse> search(EmployeeSearchCriteria criteria, Pageable pageable) {
        return employeeRepository.findAll(EmployeeSpecifications.matching(criteria), pageable)
                .map(EmployeeResponse::from);
    }
}
