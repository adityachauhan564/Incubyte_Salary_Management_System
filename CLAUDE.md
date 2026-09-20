# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This repository currently contains **no application code** — only planning artifacts:

- `Incubyte_Task.pdf` — the assessment brief (goal, constraints, grading criteria).
- `docs/requirements.md` — the one-page requirements doc (scope, non-goals, tech choices).
- `docs/architecture.md`, `docs/assumptions.md` — currently empty; fill these in as design
  decisions are made.

There is no git repository initialized yet, and no backend/frontend project scaffolding
exists. When code is added, this file should be updated with real build/lint/test commands
and the actual module layout.

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
