# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Backend is functional end-to-end for employees, salaries, and compensation analytics:
entities, repositories, DTOs, services, REST controllers, a shared error-handling advice,
and a deterministic seed mechanism all exist and are tested. No Angular UI screens yet.

### Implemented endpoints

- `GET /api/employees/{id}` — 404 via `EmployeeNotFoundException` if missing.
- `GET /api/employees?search=&country=&department=&jobTitle=&page=&size=&sort=` — paginated,
  filtered list; returns `PagedModel<EmployeeResponse>`.
- `GET /api/employees/{employeeId}/salaries` — full salary history, most recent first.
- `GET /api/employees/{employeeId}/salary/current` — derived current salary; 404 if none
  effective yet.
- `POST /api/employees/{employeeId}/salaries` — creates a new history entry (a raise); 201.
- `PUT /api/employees/{employeeId}/salaries/{salaryId}` — corrects an existing record's
  fields in place (not a new history entry); 200. Note: `employeeId` in this path is not
  currently verified to match the record's actual employee — see `docs/architecture.md`
  and the increment notes for why that's a known, deliberately-deferred gap.

- `GET /api/analytics/summary` — headcount, total cost, average/median/min/max salary, all
  normalized to USD.
- `GET /api/analytics/departments`, `/api/analytics/countries` — per-group headcount, cost,
  average/min/max (covers both "comparison" and "cost" requirements in one shape).
- `GET /api/analytics/distribution?bucketSize=25000` — fixed-width salary buckets, gap-filled
  with zero counts.

All errors return the same JSON shape (`timestamp`, `status`, `error`, `message`, `path`)
via `common/exception/GlobalExceptionHandler`; see `docs/architecture.md` §7.1. Exchange
rates for analytics normalization are documented in `docs/assumptions.md` §7.

- `Incubyte_Task.pdf` — the assessment brief (goal, constraints, grading criteria).
- `docs/requirements.md` — the one-page requirements doc (scope, non-goals, tech choices).
- `docs/architecture.md` — module structure, entity design, and implementation decisions
  (kept up to date as increments land).
- `docs/assumptions.md` — scope decisions and reasoning where requirements leave details open.
- `backend/` — Spring Boot 4.1.1 app generated via Spring Initializr (Java 21, Maven wrapper).
- `frontend/` — Angular 19 app generated via Angular CLI (routing + SCSS enabled); not yet
  wired to the backend.

### Seeding local data

`./mvnw spring-boot:run -Dspring-boot.run.profiles=seed` populates `backend/data/salary.db`
with ~10,000 synthetic employees and salary history (override with
`-Dspring-boot.run.arguments="--app.seed.employee-count=500"`). Safe to re-run — it skips
seeding if employees already exist. See `docs/architecture.md` for how the seed module is
structured.

## Commands

### Backend (`backend/`)

- Run the app: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`)
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=ClassName`
- Package: `./mvnw package`

No system-wide Maven is required — always use the wrapper (`mvnw`/`mvnw.cmd`).

### Frontend (`frontend/`)

- Install deps: `npm install`
- Dev server: `npx ng serve`
- Run unit tests: `npx ng test`
- Build: `npx ng build`

Angular CLI is not installed globally — invoke it via `npx ng ...` (or `npm run` scripts) so
the pinned local version (19.x) is used, matching the installed Node runtime.

## Backend notes

- Package root: `com.incubyte.salary`.
- SQLite is **not** a standard Spring Initializr dependency — it was wired in manually:
  `org.xerial:sqlite-jdbc` for the driver and `org.hibernate.orm:hibernate-community-dialects`
  (version managed by the Spring Boot BOM) for `org.hibernate.community.dialect.SQLiteDialect`.
- `src/main/resources/application.properties` points at `./data/salary.db` (gitignored;
  `backend/data/.gitkeep` keeps the directory present). Tests use a separate
  `src/test/resources/application.properties` pointing at `./data/salary-test.db` with
  `ddl-auto=create-drop` so tests don't touch dev data.
- Spring Boot 4 split the old single `spring-boot-starter-test` into per-module test starters
  (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`,
  `spring-boot-starter-validation-test`) — add the matching `-test` starter when a new main
  starter is introduced, rather than reaching for `spring-boot-starter-test`.

## What is being built

A **Salary Management System** for an HR Manager to manage and analyze employee
compensation data for ~10,000 synthetic employees across multiple countries/departments,
replacing spreadsheet-based salary tracking. Full requirements are in
`docs/requirements.md`; the original assessment brief is `Incubyte_Task.pdf`.

Single-user scope: authentication/authorization is explicitly out of scope.

### Planned architecture (modular monolith)

```
Angular UI → REST API → Spring Boot → Service Layer → Repository Layer → SQLite
```

A modular monolith was chosen deliberately over microservices — the domain doesn't need
independently deployable services.

### Planned technology

- **Backend**: Java 21, Spring Boot, Spring Data JPA / Hibernate, Maven, SQLite, JUnit 5, Mockito
- **Frontend**: Angular, TypeScript, RxJS
- **Tooling**: Git, Docker, CI/CD
- A seed script must populate ~10,000 synthetic employee records.

### Core functional scope

- Employee list with search, filtering, pagination, and detail view (no create/update/delete
  of employees — that's a non-goal).
- Salary records: view current salary, view history, create records, update records; each
  record retains currency and effective date.
- Compensation analytics: total headcount, total salary cost, average/median/min/max salary,
  distribution by ranges, comparisons by department and by country, cost by department and
  by country.
- Cross-country comparisons normalize currency via a documented, static exchange-rate
  configuration (no live FX integration).

### Explicit non-goals

Do not implement unless requirements change: employee deletion, full employee CRUD, salary
deletion, authentication/authorization, natural-language queries, payroll processing, tax
calculation, employee self-service, real-time FX integration.

### Non-functional expectations

- Clear separation of responsibilities (controller/service/repository layers), OO design.
- Core business logic (salary calculations, analytics aggregation) must have deterministic
  unit tests — fast, no flakiness.
- Must stay performant at ~10k records: paginate everything, filter/sort server-side, index
  appropriately, avoid N+1 queries.
- Consistent validation and error handling across the API.

## Development approach expected by the assessment

- Commit incrementally so the history shows how the solution evolved — avoid one giant commit.
- Keep artifacts (requirements, architecture notes, trade-off/assumption docs, AI prompts
  used) under `docs/` as the project develops.
- Final solution must be end-to-end functional (backend + UI) and deployable, not just
  passing tests.
