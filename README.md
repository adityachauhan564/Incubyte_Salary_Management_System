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

Functional end-to-end, backend and frontend:

- **Backend**: employees, salary history, and compensation analytics, all
  documented and tested.
- **Frontend**: employee search/browse, salary history/create/correct, and
  an analytics dashboard, all built against the live backend API and tested.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1, Maven (wrapper included) |
| Persistence | SQLite, Spring Data JPA / Hibernate |
| API docs | springdoc-openapi (Swagger UI) |
| Monitoring | Spring Boot Actuator |
| Backend testing | JUnit 5, Mockito, AssertJ |
| Frontend | Angular 19 (standalone components, Signals), TypeScript, Angular Material, ngx-charts |
| Frontend testing | Karma, Jasmine |

## Project Structure

```
backend/    Spring Boot API (Java 21, Maven)
frontend/   Angular app (Angular 19, feature-based folders)
docs/       Requirements, architecture, and assumptions
```

See [`docs/architecture.md`](docs/architecture.md) for the full backend
package layout and design rationale, and
[`docs/superpowers/specs/2026-09-20-angular-frontend-design.md`](docs/superpowers/specs/2026-09-20-angular-frontend-design.md)
for the frontend's design decisions.

The frontend mirrors the backend's package-by-feature convention:

```
frontend/src/app/
├── core/        HTTP error interceptor, TS models mirroring backend DTOs
├── employees/   Employee list (search/filter/pagination) and detail view
├── salary/      Salary service, create/correct dialog
└── analytics/   Analytics dashboard (stat cards + ngx-charts)
```

## Getting Started

### Prerequisites

- Java 21
- Node.js (no system-wide Maven or Angular CLI needed — both projects use
  their own wrappers, invoke the frontend's via `npx ng ...`)

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

### Run the backend tests

```bash
cd backend
./mvnw test
```

### Run the frontend

The backend must already be running on `http://localhost:8080` — the dev
server proxies `/api` and `/actuator` requests to it
(`frontend/proxy.conf.json`), so no CORS configuration is needed.

```bash
cd frontend
npm install
npx ng serve
```

The app starts on `http://localhost:4200`.

### Run the frontend tests

```bash
cd frontend
npx ng test
```

### Build the frontend for production

```bash
cd frontend
npx ng build
```

Output is written to `frontend/dist/frontend`.

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
- **Angular Signals + plain services** for frontend state, not NgRx — the
  app is a handful of CRUD screens and dashboards, not complex enough to
  justify a state-management library.
- **The backend is the frontend's source of truth**: all TypeScript models
  mirror the backend DTOs exactly, and a dev-server proxy
  (`frontend/proxy.conf.json`) forwards `/api`/`/actuator` calls instead of
  adding CORS configuration to the backend.

See `docs/architecture.md` for the full reasoning behind these and other
decisions.

## Known Limitations

- No authentication/authorization (explicit non-goal — single HR Manager
  user assumed).
- `PUT .../salaries/{salaryId}` does not verify that the record belongs to
  the `employeeId` in the path.
- No API versioning.
- Employee list filters (country/department/job title) are free-text
  inputs requiring an exact match, not dropdowns — there's no backend
  endpoint for distinct filter values yet.
- A malformed employee id in the URL (e.g. `/employees/abc`) surfaces as a
  backend 500 rather than a 400 (an unhandled path-variable type-mismatch
  exception); the frontend absorbs it into the same "Employee not found"
  state either way, so it isn't user-visible.
