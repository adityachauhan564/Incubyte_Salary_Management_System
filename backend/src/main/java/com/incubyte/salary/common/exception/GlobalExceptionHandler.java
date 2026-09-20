package com.incubyte.salary.common.exception;

import com.incubyte.salary.common.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Translates exceptions thrown by controllers/services into the consistent
 * {@link ApiError} JSON shape, so no stack trace or internal detail ever
 * reaches a client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiError> handleEmployeeNotFound(EmployeeNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(SalaryRecordNotFoundException.class)
    public ResponseEntity<ApiError> handleSalaryRecordNotFound(SalaryRecordNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "SALARY_RECORD_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSalaryRecordException.class)
    public ResponseEntity<ApiError> handleInvalidSalaryRecord(InvalidSalaryRecordException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_SALARY_RECORD", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(UnsupportedCurrencyException.class)
    public ResponseEntity<ApiError> handleUnsupportedCurrency(UnsupportedCurrencyException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "UNSUPPORTED_CURRENCY", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateSalaryRecordException.class)
    public ResponseEntity<ApiError> handleDuplicateSalaryRecord(DuplicateSalaryRecordException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "DUPLICATE_SALARY_RECORD", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception while processing {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest request) {
        ApiError body = new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
