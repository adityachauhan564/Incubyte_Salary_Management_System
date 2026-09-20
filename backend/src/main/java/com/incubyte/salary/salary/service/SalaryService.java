package com.incubyte.salary.salary.service;

import com.incubyte.salary.common.exception.DuplicateSalaryRecordException;
import com.incubyte.salary.common.exception.EmployeeNotFoundException;
import com.incubyte.salary.common.exception.InvalidSalaryRecordException;
import com.incubyte.salary.common.exception.SalaryRecordNotFoundException;
import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.employee.repository.EmployeeRepository;
import com.incubyte.salary.salary.dto.SalaryRecordRequest;
import com.incubyte.salary.salary.dto.SalaryRecordResponse;
import com.incubyte.salary.salary.entity.SalaryRecord;
import com.incubyte.salary.salary.repository.SalaryRecordRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SalaryService {

    private final SalaryRecordRepository salaryRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final Validator validator;
    private final Clock clock;

    public SalaryService(SalaryRecordRepository salaryRecordRepository,
                          EmployeeRepository employeeRepository,
                          Validator validator,
                          Clock clock) {
        this.salaryRecordRepository = salaryRecordRepository;
        this.employeeRepository = employeeRepository;
        this.validator = validator;
        this.clock = clock;
    }

    public List<SalaryRecordResponse> getHistory(Long employeeId) {
        requireEmployee(employeeId);
        return salaryRecordRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId).stream()
                .map(SalaryRecordResponse::from)
                .toList();
    }

    public SalaryRecordResponse getCurrentSalary(Long employeeId) {
        requireEmployee(employeeId);
        SalaryRecord current = salaryRecordRepository
                .findFirstByEmployeeIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(employeeId, LocalDate.now(clock))
                .orElseThrow(() -> SalaryRecordNotFoundException.noCurrentSalaryFor(employeeId));
        return SalaryRecordResponse.from(current);
    }

    /**
     * Adds a new point in the employee's salary history (e.g. a raise).
     * Never overwrites an existing record.
     */
    @Transactional
    public SalaryRecordResponse createSalaryRecord(Long employeeId, SalaryRecordRequest request) {
        validate(request);
        Employee employee = requireEmployee(employeeId);

        SalaryRecord record = new SalaryRecord(employee, request.amount(), request.currency(), request.effectiveDate());
        return SalaryRecordResponse.from(saveOrThrowOnDuplicate(record));
    }

    /**
     * Corrects the fields of an existing salary record in place. This does
     * not add a new history entry - use {@link #createSalaryRecord} for that.
     */
    @Transactional
    public SalaryRecordResponse updateSalaryRecord(Long salaryRecordId, SalaryRecordRequest request) {
        validate(request);

        SalaryRecord record = salaryRecordRepository.findById(salaryRecordId)
                .orElseThrow(() -> SalaryRecordNotFoundException.byId(salaryRecordId));

        record.setAmount(request.amount());
        record.setCurrency(request.currency());
        record.setEffectiveDate(request.effectiveDate());

        return SalaryRecordResponse.from(saveOrThrowOnDuplicate(record));
    }

    private Employee requireEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    /**
     * The (employee, effectiveDate) uniqueness rule is enforced by a
     * database constraint rather than a pre-check here, which avoids a
     * check-then-act race; this just translates the resulting failure into
     * a clear client error. Catches the broader DataAccessException rather
     * than DataIntegrityViolationException specifically: the SQLite dialect
     * in use here doesn't classify the underlying constraint violation into
     * that subtype and instead surfaces it as a JpaSystemException (verified
     * empirically) - nothing else can plausibly fail at this exact save.
     */
    private SalaryRecord saveOrThrowOnDuplicate(SalaryRecord record) {
        try {
            return salaryRecordRepository.saveAndFlush(record);
        } catch (DataAccessException e) {
            throw new DuplicateSalaryRecordException(record.getEmployee().getId(), record.getEffectiveDate());
        }
    }

    private void validate(SalaryRecordRequest request) {
        Set<ConstraintViolation<SalaryRecordRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining("; "));
            throw new InvalidSalaryRecordException(message);
        }
    }
}
