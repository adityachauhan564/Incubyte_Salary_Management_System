package com.incubyte.salary.salary.entity;

import com.incubyte.salary.employee.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * The "one salary per employee per day" rule is enforced by a unique index
 * on (employee_id, effective_date), which also serves as the composite
 * index the current-salary/history queries need. It is NOT declared here
 * via @Table(uniqueConstraints=...) or @Index(unique=true): both route
 * through Hibernate's dialect-level unique-constraint support, which the
 * SQLite community dialect does not implement (SQLite has no ALTER TABLE
 * ADD CONSTRAINT), so Hibernate silently emits no DDL for either - verified
 * empirically, not merely assumed. The index is instead created directly in
 * schema.sql (see src/main/resources and src/test/resources), which Spring
 * Boot runs right after Hibernate creates the tables.
 */
@Entity
@Table(name = "salary_records")
public class SalaryRecord {





    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO 4217 code")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SalaryRecord() {
        // JPA
    }

    public SalaryRecord(Employee employee, BigDecimal amount, String currency, LocalDate effectiveDate) {
        this.employee = employee;
        this.amount = amount;
        this.currency = currency;
        this.effectiveDate = effectiveDate;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SalaryRecord other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
