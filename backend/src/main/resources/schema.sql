-- Hibernate's SQLite dialect cannot express unique constraints/indexes
-- (see SalaryRecord's Javadoc), so this is created directly here instead.
-- Runs after Hibernate creates the tables (spring.jpa.defer-datasource-initialization=true).
CREATE UNIQUE INDEX IF NOT EXISTS uk_salary_record_employee_effective_date
    ON salary_records (employee_id, effective_date);
