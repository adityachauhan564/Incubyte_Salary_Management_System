package com.incubyte.salary.seed;

import com.incubyte.salary.employee.entity.Employee;
import com.incubyte.salary.salary.entity.SalaryRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Deterministic generator for synthetic employee + salary data. Pure and
 * side-effect free: calling {@link #generate(int, long)} with the same
 * arguments always returns the same data, which is what makes the seed
 * reproducible and lets it be unit-tested without a database.
 */
@Component
public class SeedDataFactory {

    /**
     * Every generated salary record is effective on or before this fixed
     * date, so a current salary is always derivable no matter when the seed
     * is actually run.
     */
    static final LocalDate REFERENCE_DATE = LocalDate.of(2025, 1, 1);

    private static final String[] FIRST_NAMES = {
            "Ada", "Grace", "Alan", "Katherine", "Linus", "Margaret", "John", "Barbara",
            "Dennis", "Radia", "Donald", "Frances", "Edsger", "Shafi", "Tim", "Marissa",
            "Guido", "Anita", "Ken", "Sophie", "James", "Elena", "Michael", "Priya",
            "David", "Fatima", "Robert", "Chen", "Daniel", "Amara", "Thomas", "Yuki",
            "Richard", "Ingrid", "Peter", "Noor", "Steven", "Camille", "Mark", "Aisha"
    };

    private static final String[] LAST_NAMES = {
            "Lovelace", "Hopper", "Turing", "Johnson", "Torvalds", "Hamilton", "McCarthy", "Liskov",
            "Ritchie", "Perlman", "Knuth", "Allen", "Dijkstra", "Goldwasser", "Berners-Lee", "Mayer",
            "van Rossum", "Borg", "Thompson", "Wilson", "Clarke", "Petrova", "Chen", "Sharma",
            "Kim", "Al-Farsi", "Garcia", "Wei", "Novak", "Okafor", "Muller", "Tanaka",
            "Brown", "Larsen", "Schmidt", "Haddad", "Nguyen", "Dubois", "Taylor", "Khan"
    };

    private static final Map<String, String> COUNTRY_CURRENCIES = Map.ofEntries(
            Map.entry("United States", "USD"),
            Map.entry("United Kingdom", "GBP"),
            Map.entry("Germany", "EUR"),
            Map.entry("France", "EUR"),
            Map.entry("Netherlands", "EUR"),
            Map.entry("India", "INR"),
            Map.entry("Canada", "CAD"),
            Map.entry("Australia", "AUD"),
            Map.entry("Singapore", "SGD"),
            Map.entry("Brazil", "BRL")
    );

    private static final Map<String, Double> CURRENCY_MULTIPLIERS = Map.ofEntries(
            Map.entry("USD", 1.0),
            Map.entry("GBP", 0.8),
            Map.entry("EUR", 0.9),
            Map.entry("INR", 83.0),
            Map.entry("CAD", 1.35),
            Map.entry("AUD", 1.5),
            Map.entry("SGD", 1.3),
            Map.entry("BRL", 5.3)
    );

    private static final Map<String, List<String>> DEPARTMENT_JOB_TITLES = Map.ofEntries(
            Map.entry("Engineering", List.of("Software Engineer", "Senior Software Engineer", "Engineering Manager")),
            Map.entry("Sales", List.of("Sales Representative", "Account Executive", "Sales Manager")),
            Map.entry("Marketing", List.of("Marketing Specialist", "Marketing Manager", "Content Strategist")),
            Map.entry("Human Resources", List.of("HR Generalist", "HR Business Partner", "Recruiter")),
            Map.entry("Finance", List.of("Financial Analyst", "Accountant", "Finance Manager")),
            Map.entry("Operations", List.of("Operations Analyst", "Operations Manager", "Logistics Coordinator")),
            Map.entry("Customer Support", List.of("Support Specialist", "Support Team Lead")),
            Map.entry("Product", List.of("Product Manager", "Product Analyst")),
            Map.entry("Legal", List.of("Legal Counsel", "Compliance Officer")),
            Map.entry("IT", List.of("IT Support Specialist", "Systems Administrator"))
    );

    private static final List<String> COUNTRIES = List.copyOf(COUNTRY_CURRENCIES.keySet());
    private static final List<String> DEPARTMENTS = List.copyOf(DEPARTMENT_JOB_TITLES.keySet());

    public List<GeneratedEmployee> generate(int count, long randomSeed) {
        Random random = new Random(randomSeed);
        List<GeneratedEmployee> employees = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String firstName = pick(FIRST_NAMES, random);
            String lastName = pick(LAST_NAMES, random);
            String country = pick(COUNTRIES, random);
            String department = pick(DEPARTMENTS, random);
            String jobTitle = pick(DEPARTMENT_JOB_TITLES.get(department), random);

            Employee employee = new Employee(firstName, lastName, country, department, jobTitle);
            List<SalaryRecord> history = generateSalaryHistory(employee, country, random);

            employees.add(new GeneratedEmployee(employee, history));
        }

        return employees;
    }

    private List<SalaryRecord> generateSalaryHistory(Employee employee, String country, Random random) {
        String currency = COUNTRY_CURRENCIES.get(country);
        double multiplier = CURRENCY_MULTIPLIERS.get(currency);

        // ~30% of employees have a raise history of 2-4 records; the rest have exactly one.
        int recordCount = random.nextInt(10) < 3 ? 2 + random.nextInt(3) : 1;
        BigDecimal baseAmount = randomBaseAmount(random, multiplier);

        List<SalaryRecord> records = new ArrayList<>(recordCount);
        for (int i = 0; i < recordCount; i++) {
            LocalDate effectiveDate = REFERENCE_DATE.minusYears(recordCount - 1L - i);
            BigDecimal amount = baseAmount
                    .add(baseAmount.multiply(BigDecimal.valueOf(0.05 * i)))
                    .setScale(2, RoundingMode.HALF_UP);
            records.add(new SalaryRecord(employee, amount, currency, effectiveDate));
        }

        return records;
    }

    private BigDecimal randomBaseAmount(Random random, double multiplier) {
        double baseInUsd = 45_000 + random.nextInt(90_000);
        return BigDecimal.valueOf(baseInUsd * multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private static String pick(String[] values, Random random) {
        return values[random.nextInt(values.length)];
    }

    private static String pick(List<String> values, Random random) {
        return values.get(random.nextInt(values.size()));
    }
}
