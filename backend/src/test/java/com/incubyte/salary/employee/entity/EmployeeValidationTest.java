package com.incubyte.salary.employee.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeValidationTest {

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
    void isValidWhenAllRequiredFieldsArePresent() {
        Employee employee = new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", "Software Engineer");

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void isInvalidWhenFirstNameIsBlank(String firstName) {
        Employee employee = new Employee(firstName, "Lovelace", "United Kingdom", "Engineering", "Software Engineer");

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("firstName");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void isInvalidWhenLastNameIsBlank(String lastName) {
        Employee employee = new Employee("Ada", lastName, "United Kingdom", "Engineering", "Software Engineer");

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("lastName");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void isInvalidWhenCountryIsBlank(String country) {
        Employee employee = new Employee("Ada", "Lovelace", country, "Engineering", "Software Engineer");

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("country");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void isInvalidWhenDepartmentIsBlank(String department) {
        Employee employee = new Employee("Ada", "Lovelace", "United Kingdom", department, "Software Engineer");

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("department");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void isInvalidWhenJobTitleIsBlank(String jobTitle) {
        Employee employee = new Employee("Ada", "Lovelace", "United Kingdom", "Engineering", jobTitle);

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("jobTitle");
    }
}
