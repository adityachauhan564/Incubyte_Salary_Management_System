# Salary Management System — Architecture

## 1. Architecture Overview

The application will be implemented as a modular monolith with a clear separation
between frontend, API, application/business logic, and persistence.

```text
┌──────────────────────────────────────────────┐
│                 Angular UI                   │
│                                              │
│ Employee Management | Salary | Analytics     │
└──────────────────────┬───────────────────────┘
                       │ HTTP/JSON
                       ▼
┌──────────────────────────────────────────────┐
│              Spring Boot REST API            │
│                                              │
│ Employee | Salary | Analytics Controllers    │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│             Application / Service Layer      │
│                                              │
│ EmployeeService                              │
│ SalaryService                                │
│ AnalyticsService                             │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│               Repository Layer               │
│                                              │
│ Spring Data JPA / Hibernate                  │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│                    SQLite                    │
└──────────────────────────────────────────────┘

## 2. Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Maven
- SQLite

### Frontend

- Angular
- TypeScript
- RxJS

### Testing

- JUnit 5
- Mockito
- Spring Boot Test

### Development and Deployment

- Git
- Docker
- CI/CD
## 3. Architectural Decision — Modular Monolith

The application will be implemented as a modular monolith.

The system will have clear business modules for:

- Employee management
- Salary management
- Compensation analytics

The modules will run within a single Spring Boot application.

### Why

The assessment does not require independently deployable services.
Approximately 10,000 employee records do not justify the operational
complexity of a distributed architecture.

A modular monolith provides:

- Simpler local development
- Simpler deployment
- Easier debugging
- Easier testing
- Lower infrastructure complexity
- Clear separation of business responsibilities

Microservices are therefore intentionally not introduced.
## 4. Backend Module Structure

The backend will be organized around business capabilities while maintaining
clear separation of responsibilities.

```text
employee/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/

salary/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/

analytics/
├── controller/
├── dto/
└── service/

common/
├── exception/
├── validation/
└── response/

seed/
├── SeedDataFactory   (pure, deterministic data generation - no Spring/DB)
└── DataSeeder        (@Profile("seed") CommandLineRunner; persists what the factory generates)

The seed module is deliberately separate from the employee/salary modules'
services: seeding is a one-time bulk data-loading concern, not a
user-driven business operation, so it talks to the repositories directly
and skips per-record validation overhead. `SeedDataFactory` takes a
`(count, randomSeed)` pair and returns fully-formed but unpersisted
employees/salary records - it has no Spring dependencies, so its
invariants (determinism, required fields, valid currencies, at least one
current-effective salary record per employee) are unit-tested without a
database. `DataSeeder` is only active under the `seed` Spring profile and
skips seeding entirely if employees already exist, so it is safe to run
more than once against the same database.

Run locally with: `./mvnw spring-boot:run -Dspring-boot.run.profiles=seed`

### 4.1 Analytics Data Access

Every analytic depends on "each employee's current salary" - computing that
one row at a time (as `SalaryService` does for a single employee) would be
a 10,000-query N+1 for org-wide analytics. Instead, `SalaryRecordRepository`
exposes one additional query, `findCurrentSalarySnapshots(asOfDate)`, which
finds the latest effective record per employee in one indexed correlated
subquery on `(employee_id, effective_date)` and returns a thin projection
(`country, department, amount, currency`) - not full entities. That is the
"push aggregation to the database" part.

Currency normalization and the final numeric aggregation (sum, average,
median, min/max, grouping, bucketing) happen in `AnalyticsService` over that
already-reduced, ~one-row-per-employee list. This is deliberate, not a
shortcut: SQLite has no native median/percentile function, and normalizing
in SQL would mean duplicating the exchange-rate table into a SQL `CASE`
expression, breaking "one configured source of truth" for rates. At the
current scale (~10k rows of `(String, String, BigDecimal, String)`), this
Java-side pass is inexpensive.

## 5. Layer Responsibilities

### Controller

Responsible for:

- Handling HTTP requests
- Validating incoming request data
- Mapping requests to application/service operations
- Mapping service results to appropriate HTTP responses

Controllers should remain thin and should not contain business logic.

### Service

Responsible for:

- Application workflows
- Business rules and validations that require business context
- Coordinating repositories
- Employee operations
- Salary operations
- Compensation analytics

Services should contain the core application/business logic and should not
depend on HTTP-specific concerns.

Two implementation decisions worth recording:

- **Validation without a controller.** `SalaryRecordRequest`'s Bean
  Validation annotations (`@NotNull`, `@DecimalMin`, currency `@Pattern`)
  are the single source of truth for "what makes a salary record valid."
  Since no controller exists yet to trigger `@Valid` automatically,
  `SalaryService` validates requests itself by injecting the standard
  `jakarta.validation.Validator` bean and checking for violations before
  touching the repository. When a controller is added, it can rely on the
  same annotations via `@Valid` without duplicating any rules.
- **Deterministic "today."** Current-salary derivation depends on
  "effective on or before today." `SalaryService` takes a `java.time.Clock`
  bean (`Clock.systemDefaultZone()` in production) rather than calling
  `LocalDate.now()` directly, so tests can inject `Clock.fixed(...)` and
  assert exact behavior without depending on the real calendar date.

### Repository

Responsible for:

- Persistence
- Entity retrieval and storage
- Database queries
- Database-level filtering, sorting, and aggregation where appropriate

Repositories should not contain application or business workflows.

### DTO

DTOs define the API boundary between the frontend and backend.

They are used to:

- Represent request data
- Represent response data
- Control which fields are exposed through the API
- Separate the API contract from the persistence model

JPA entities should not be exposed directly through REST endpoints.

## 6. Database & Domain Model

The system will use SQLite with Spring Data JPA and Hibernate.

The primary domain entities are:

- Employee
- Salary

An employee can have multiple salary records over time.

```text
Employee
   │
   │ 1
   │
   │ *
Salary

### 6.1 Employee Entity

`Employee` (table `employees`) represents an employee record used for search,
filtering, and salary association. It has no status/active flag: employee
deletion and full CRUD are out of scope, so employees are populated by the
seed process and are otherwise read-only at this stage.

| Field      | Type   | Notes                                        |
|------------|--------|-----------------------------------------------|
| id         | Long   | Generated primary key                         |
| firstName  | String | Required                                      |
| lastName   | String | Required; indexed for search                  |
| country    | String | Required; indexed for filtering/analytics     |
| department | String | Required; indexed for filtering/analytics     |
| jobTitle   | String | Required; indexed for filtering               |

Country and department are plain indexed string columns rather than separate
lookup entities or enums: at the current scope they are descriptive
attributes of an employee, not managed entities with their own lifecycle, and
a string column keeps seeding and querying simple.

### 6.2 SalaryRecord Entity

`SalaryRecord` (table `salary_records`) is the single source of truth for an
employee's compensation. There is no separate `currentSalary` field anywhere
— it is always derived from history.

| Field         | Type          | Notes                                            |
|---------------|---------------|----------------------------------------------------|
| id            | Long          | Generated primary key                              |
| employee      | Employee (FK) | `employee_id`, required, indexed                   |
| amount        | BigDecimal    | Required, > 0, `NUMERIC(19,2)`                     |
| currency      | String        | Required, 3-letter ISO 4217 code (e.g. `USD`)      |
| effectiveDate | LocalDate     | Required; date the amount takes effect             |
| createdAt     | Instant       | Set once on insert; audit timestamp, not editable  |

A unique index on `(employee_id, effective_date)` enforces "one salary per
employee per day" and doubles as the composite index both required read
patterns need:

- **Current salary** — the `SalaryRecord` for an employee with the latest
  `effectiveDate <= today` (a future-dated record is not yet current). Ties
  (blocked going forward by the constraint, but not impossible for data that
  predates it) are broken by the highest id, both here and in the bulk
  analytics query - one rule, applied consistently.
- **Salary history** — all of an employee's `SalaryRecord`s ordered by
  `effectiveDate` descending.

That index is created in `schema.sql`, not via `@Table(uniqueConstraints=...)`
or `@Index(unique=true)` on the entity: Hibernate routes both through the
SQLite dialect's unique-constraint support, which is a no-op here (SQLite has
no `ALTER TABLE ADD CONSTRAINT`) - verified empirically, both silently
produced no DDL at all. `schema.sql` runs immediately after Hibernate creates
the tables (`spring.jpa.defer-datasource-initialization=true`) and is
idempotent (`CREATE UNIQUE INDEX IF NOT EXISTS`), so it's safe against the
dev datasource's `ddl-auto=update` running on every startup. A duplicate
insert violates the constraint but isn't classified as
`DataIntegrityViolationException` by this dialect either - `SalaryService`
catches the broader `DataAccessException` and translates it into
`DuplicateSalaryRecordException` (409).

`BigDecimal` is used for `amount`, never floating point, per the Monetary
Values assumption. Currency is stored as its native ISO code; normalizing
into a common reporting currency for cross-country analytics is a read-time
concern handled by a separate, non-persisted exchange-rate configuration —
it is not stored on `SalaryRecord`. Because SQLite/Hibernate can hand back a
whole-number amount with scale 0, `SalaryRecordResponse.from()` explicitly
rescales to 2 decimals so the API always represents money consistently
(e.g. always `95000.00`, never sometimes `95000`).

### 6.3 Relationship

An `Employee` has many `SalaryRecord`s, but `Employee` does not hold a
`@OneToMany` collection of them. Salary records are always queried directly
through `SalaryRecordRepository` by `employeeId`, which avoids
lazy-collection pitfalls and keeps `Employee` fetches cheap regardless of how
much salary history an employee has accumulated.

### 7. API & Data Flow

```markdown
## 7. API & Data Flow

The frontend communicates with the backend through REST APIs using JSON.

Example flow:

```text
Angular Component
       ↓
Angular Service
       ↓
HTTP Request
       ↓
Spring Boot Controller
       ↓
Application/Service Layer
       ↓
Repository
       ↓
SQLite
       ↓
Repository
       ↓
Service
       ↓
DTO
       ↓
REST Response
       ↓
Angular Service
       ↓
Angular Component

### 7.1 API Conventions

Two response shapes are now fixed across the API so future controllers stay
consistent with the Employee/Salary ones:

- **Errors** — every handled failure returns the same JSON shape
  (`timestamp`, `status`, `error` code, `message`, `path`), produced by one
  shared `@RestControllerAdvice`. No stack trace or internal detail is ever
  returned to a client; unexpected exceptions are logged server-side and
  reported to the client as a generic `INTERNAL_ERROR`.
- **Paginated lists** — returned as Spring Data's `PagedModel<T>`
  (`{ "content": [...], "page": { "size", "totalElements", "totalPages",
  "number" } }`) rather than serializing `Page<T>` directly, which leaks
  internal Spring Data structure and isn't a stable contract.

## 8. Performance Considerations

The application is expected to handle approximately 10,000 employees.

The implementation will:

- Use server-side pagination
- Perform filtering and sorting at the database level
- Avoid unnecessary loading of the complete dataset
- Avoid N+1 queries
- Use appropriate database indexes
- Perform aggregation at the database level where practical

Performance optimizations should remain proportional to the assessment scope.
Additional infrastructure such as Redis or distributed caching will not be
introduced without a demonstrated requirement.

## 9. Testing Strategy

Testing will focus on business behavior and important system boundaries.

### Unit Tests

Unit tests will cover core business logic, including:

- Salary creation and updates
- Salary validation
- Salary history
- Currency normalization
- Compensation calculations
- Department comparisons
- Country comparisons

JUnit 5 and Mockito will be used for isolated unit tests.

### Integration Tests

Integration tests will verify important interactions between:

- REST controllers
- Service layer
- Repositories
- SQLite database

The goal is to test observable behavior rather than implementation details.

Tests should be deterministic, repeatable, and independent of external
services.