# Salary Management System

A web application for an HR Manager to search employees, manage salary
history, and analyze compensation across departments and countries —
replacing spreadsheet-based salary tracking for an organization of
~10,000 employees.

Built as part of a software engineering assessment. See
[`Incubyte_Task.pdf`](Incubyte_Task.pdf) for the original brief and
[`docs/`](docs) for the requirements, architecture, and assumptions that
guided the implementation.

## Status

- **Backend**: functional end-to-end — employees, salary history, and
  compensation analytics, all documented and tested.
- **Frontend**: not yet implemented (Angular CLI scaffold only).

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1, Maven (wrapper included) |
| Persistence | SQLite, Spring Data JPA / Hibernate |
| API docs | springdoc-openapi (Swagger UI) |
| Monitoring | Spring Boot Actuator |
| Testing | JUnit 5, Mockito, AssertJ |
| Frontend (planned) | Angular 19, TypeScript, RxJS |

## Project Structure

```
backend/    Spring Boot API (Java 21, Maven)
frontend/   Angular app (scaffold only, not yet built out)
docs/       Requirements, architecture, and assumptions
```

See [`docs/architecture.md`](docs/architecture.md) for the full backend
package layout and design rationale.

## Getting Started

### Prerequisites

- Java 21
- Node.js (for the frontend scaffold; no system-wide Maven or Angular CLI
  needed — both projects use their own wrappers)

### Run the backend

```bash
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`.

### Seed sample data

The database starts empty. To populate it with ~10,000 deterministic
synthetic employees and salary records:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

Safe to re-run — seeding is skipped if employees already exist. Override
the count with `-Dspring-boot.run.arguments="--app.seed.employee-count=500"`.

### Run the tests

```bash
cd backend
./mvnw test
```

### Frontend (scaffold)

```bash
cd frontend
npm install
npx ng serve
```

## API Documentation

Once the backend is running:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Health check**: http://localhost:8080/actuator/health

## API Overview

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/employees/{id}` | Get one employee |
| GET | `/api/employees` | Search/filter employees, paginated |
| GET | `/api/employees/{employeeId}/salaries` | Full salary history |
| GET | `/api/employees/{employeeId}/salary/current` | Current (derived) salary |
| POST | `/api/employees/{employeeId}/salaries` | Create a salary record (a raise) |
| PUT | `/api/employees/{employeeId}/salaries/{salaryId}` | Correct an existing record |
| GET | `/api/analytics/summary` | Org-wide compensation summary |
| GET | `/api/analytics/departments` / `/countries` | Comparison and cost by group |
| GET | `/api/analytics/distribution` | Salary distribution by range |

Every error response uses a consistent shape:
`{ timestamp, status, error, message, path }`. Full request/response
schemas and status codes are documented in Swagger UI.

## Documentation

- [`docs/requirements.md`](docs/requirements.md) — scope, goals, and
  explicit non-goals
- [`docs/architecture.md`](docs/architecture.md) — module structure,
  domain model, and recorded design decisions
- [`docs/assumptions.md`](docs/assumptions.md) — reasoning behind every
  place the requirements left a detail open
- [`CLAUDE.md`](CLAUDE.md) — repository guide for AI-assisted development

## Key Design Decisions

- **Current salary is derived, never stored** — always computed from
  salary history, so it can never drift out of sync with it.
- **Salary history is append-only** — creating a record adds a new point
  in history (e.g. a raise); updating one corrects an existing record's
  fields in place without creating a duplicate.
- **A database-level unique index** prevents two salary records for the
  same employee on the same effective date.
- **Currency normalization** for cross-country analytics uses a static,
  documented exchange-rate table — no live FX integration.
- **Modular monolith**, organized by feature (`employee/`, `salary/`,
  `analytics/`) rather than by technical layer.

See `docs/architecture.md` for the full reasoning behind these and other
decisions.

## Known Limitations

- No authentication/authorization (explicit non-goal — single HR Manager
  user assumed).
- `PUT .../salaries/{salaryId}` does not verify that the record belongs to
  the `employeeId` in the path.
- No API versioning.
- Frontend is not yet implemented.
