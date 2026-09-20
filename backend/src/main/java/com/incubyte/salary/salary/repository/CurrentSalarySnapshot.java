package com.incubyte.salary.salary.repository;

import java.math.BigDecimal;

/**
 * Thin projection of one employee's current salary, used only for bulk
 * analytics calculations - deliberately not an API-facing DTO and not a
 * full entity graph.
 */
public record CurrentSalarySnapshot(String country, String department, BigDecimal amount, String currency) {
}
