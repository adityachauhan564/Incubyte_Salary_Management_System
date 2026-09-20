package com.incubyte.salary.salary.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryRecordRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

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
    void isValidWhenAllFieldsArePresentAndCorrect() {
        SalaryRecordRequest request = new SalaryRecordRequest(new BigDecimal("95000.00"), "USD", LocalDate.of(2026, 1, 1));

        Set<ConstraintViolation<SalaryRecordRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void isInvalidWhenAmountIsNotPositive() {
        SalaryRecordRequest request = new SalaryRecordRequest(BigDecimal.ZERO, "USD", LocalDate.of(2026, 1, 1));

        Set<ConstraintViolation<SalaryRecordRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("amount");
    }

    @Test
    void isInvalidWhenCurrencyIsNotAThreeLetterIsoCode() {
        SalaryRecordRequest request = new SalaryRecordRequest(new BigDecimal("95000.00"), "usd", LocalDate.of(2026, 1, 1));

        Set<ConstraintViolation<SalaryRecordRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("currency");
    }

    @Test
    void isInvalidWhenEffectiveDateIsMissing() {
        SalaryRecordRequest request = new SalaryRecordRequest(new BigDecimal("95000.00"), "USD", null);

        Set<ConstraintViolation<SalaryRecordRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("effectiveDate");
    }
}
