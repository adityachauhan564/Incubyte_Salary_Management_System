package com.incubyte.salary.salary.entity;

import com.incubyte.salary.employee.entity.Employee;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryRecordValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    private final Employee employee = new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer");

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    void isValidWhenAllRequiredFieldsArePresent() {
        SalaryRecord record = new SalaryRecord(employee, new BigDecimal("95000.00"), "USD", LocalDate.of(2026, 1, 1));

        Set<ConstraintViolation<SalaryRecord>> violations = validator.validate(record);

        assertThat(violations).isEmpty();
    }

    @Test
    void isInvalidWhenEmployeeIsMissing() {
        SalaryRecord record = new SalaryRecord(null, new BigDecimal("95000.00"), "USD", LocalDate.of(2026, 1, 1));

        Set<ConstraintViolation<SalaryRecord>> violations = validator.validate(record);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("employee");
    }

    @Test
    void isInvalidWhenAmountIsMissing() {
        SalaryRecord record = new SalaryRecord(employee, null, "USD", LocalDate.of(2026, 1, 1));

        Set<ConstraintViolation<SalaryRecord>> violations = validator.validate(record);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("amount");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-1.00"})
    void isInvalidWhenAmountIsNotPositive(String amount) {
        SalaryRecord record = new SalaryRecord(employee, new BigDecimal(amount), "USD", LocalDate.of(2026, 1, 1));

        Set<ConstraintViolation<SalaryRecord>> violations = validator.validate(record);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("amount");
    }

    @ParameterizedTest
    @ValueSource(strings = {"usd", "US", "USDD", "123"})
    void isInvalidWhenCurrencyIsNotAThreeLetterIsoCode(String currency) {
        SalaryRecord record = new SalaryRecord(employee, new BigDecimal("95000.00"), currency, LocalDate.of(2026, 1, 1));

        Set<ConstraintViolation<SalaryRecord>> violations = validator.validate(record);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("currency");
    }

    @Test
    void isInvalidWhenEffectiveDateIsMissing() {
        SalaryRecord record = new SalaryRecord(employee, new BigDecimal("95000.00"), "USD", null);

        Set<ConstraintViolation<SalaryRecord>> violations = validator.validate(record);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("effectiveDate");
    }
}
