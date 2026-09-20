package com.incubyte.salary.seed;

import com.incubyte.salary.employee.repository.EmployeeRepository;
import com.incubyte.salary.salary.repository.SalaryRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Populates the database with synthetic employees and salary history for
 * local development/demo. Only active under the "seed" Spring profile, and
 * intentionally talks to the repositories directly rather than the
 * services: seeding is a bulk data-loading concern, not a user-driven
 * business operation, so it skips per-record validation overhead.
 *
 * Run with: ./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
 */
@Component
@Profile("seed")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int LOG_EVERY = 1000;

    private final EmployeeRepository employeeRepository;
    private final SalaryRecordRepository salaryRecordRepository;
    private final SeedDataFactory seedDataFactory;

    @Value("${app.seed.employee-count:10000}")
    private int employeeCount;

    @Value("${app.seed.random-seed:42}")
    private long randomSeed;

    public DataSeeder(EmployeeRepository employeeRepository,
                       SalaryRecordRepository salaryRecordRepository,
                       SeedDataFactory seedDataFactory) {
        this.employeeRepository = employeeRepository;
        this.salaryRecordRepository = salaryRecordRepository;
        this.seedDataFactory = seedDataFactory;
    }

    @Override
    @Transactional
    public void run(String... args) {
        long existing = employeeRepository.count();
        if (existing > 0) {
            log.info("Seed skipped: {} employees already present.", existing);
            return;
        }

        log.info("Seeding {} employees (randomSeed={})...", employeeCount, randomSeed);
        List<GeneratedEmployee> generated = seedDataFactory.generate(employeeCount, randomSeed);

        int savedEmployees = 0;
        int savedSalaryRecords = 0;
        for (GeneratedEmployee entry : generated) {
            employeeRepository.save(entry.employee());
            salaryRecordRepository.saveAll(entry.salaryRecords());

            savedEmployees++;
            savedSalaryRecords += entry.salaryRecords().size();
            if (savedEmployees % LOG_EVERY == 0) {
                log.info("Seeded {}/{} employees...", savedEmployees, employeeCount);
            }
        }

        log.info("Seed complete: {} employees, {} salary records.", savedEmployees, savedSalaryRecords);
    }
}
