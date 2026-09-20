package com.incubyte.salary.salary.repository;

import com.incubyte.salary.salary.entity.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

    /**
     * The record that is in effect for the employee as of the given date: the
     * most recent record whose effectiveDate is on or before it. Passing
     * {@link LocalDate#now()} yields the current salary. Ties on effectiveDate
     * (nothing prevents two records for one employee sharing a date - a
     * unique constraint blocks new ones, but a single-column sort alone
     * wouldn't be deterministic) are broken by the highest id, the same rule
     * {@link #findCurrentSalarySnapshots} uses for bulk analytics.
     */
    Optional<SalaryRecord> findFirstByEmployeeIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
            Long employeeId, LocalDate asOfDate);

    List<SalaryRecord> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);

    /**
     * Every employee's current salary (the latest record effective on or
     * before {@code asOfDate}) as a thin projection, computed with one
     * indexed correlated subquery rather than one lookup per employee.
     * Used only for bulk analytics - never returns full entities.
     *
     * Nothing prevents two records for the same employee from sharing an
     * effectiveDate (no uniqueness constraint), so ties are broken by the
     * highest id (the most recently created record wins) to guarantee
     * exactly one row per employee - otherwise a tied employee would be
     * double-counted in every analytic.
     */
    @Query("""
            SELECT new com.incubyte.salary.salary.repository.CurrentSalarySnapshot(
                e.country, e.department, sr.amount, sr.currency)
            FROM SalaryRecord sr
            JOIN sr.employee e
            WHERE sr.effectiveDate <= :asOfDate
            AND NOT EXISTS (
                SELECT 1 FROM SalaryRecord sr2
                WHERE sr2.employee = sr.employee
                AND sr2.effectiveDate <= :asOfDate
                AND (sr2.effectiveDate > sr.effectiveDate
                    OR (sr2.effectiveDate = sr.effectiveDate AND sr2.id > sr.id))
            )
            """)
    List<CurrentSalarySnapshot> findCurrentSalarySnapshots(@Param("asOfDate") LocalDate asOfDate);
}
