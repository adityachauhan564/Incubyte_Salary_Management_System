# Salary Management System — Requirements

## 1. Problem Statement

The organization needs a system that enables an HR Manager to manage
employee salary information and understand salary costs, distribution,
and comparisons across countries and departments.

The system should replace fragmented salary management with a
centralized and understandable interface for viewing, maintaining,
and analyzing compensation data.

---

## 2. Primary User

The primary user is an HR Manager.

Authentication and authorization are outside the scope of this
assessment. The application assumes a single HR Manager user.

---

## 3. Goals

The system should allow the HR Manager to:

- View employee salary information.
- Create salary records.
- Update salary records.
- View salary history.
- Search and filter employees.
- Analyze salary costs.
- Analyze salary distribution.
- Compare salaries across departments.
- Compare salaries across countries.
- Understand overall compensation patterns.

---

## 4. Functional Requirements

### Employee Information

- View employee list.
- Search employees.
- Filter employees by relevant attributes.
- View employee details.
- Support pagination.

### Salary Management

- View current salary.
- View salary history.
- Create salary records.
- Update salary records.
- Maintain salary currency and effective date.

### Compensation Analytics

The system will provide:

- Total employee count.
- Total salary cost.
- Average salary.
- Median salary.
- Minimum and maximum salary.
- Salary distribution by ranges.
- Salary comparison by department.
- Salary comparison by country.
- Salary cost by department.
- Salary cost by country.

### Currency

Salary records will retain their native currency.

For cross-country comparisons, the system will support normalization
into a common currency using a documented exchange-rate configuration.

Real-time foreign-exchange integration is outside the scope of the MVP.

---

## 5. Non-Goals

The following features are intentionally excluded:

- Employee deletion.
- Full employee CRUD.
- Salary deletion.
- Authentication and authorization.
- Natural-language compensation queries.
- Payroll processing.
- Tax calculation.
- Employee self-service.
- Real-time foreign-exchange integration.

These exclusions keep the solution focused on the core salary-management
and compensation-analysis problem.

---

## 6. Dataset

The system will contain approximately 10,000 synthetic employee records.

The dataset will include:

- Employee information.
- Country.
- Department.
- Job title.
- Salary information.
- Currency.
- Salary effective dates.
- Salary history where applicable.

---

## 7. Non-Functional Requirements

### Maintainability

The codebase should have clear separation of responsibilities and
follow clean code and object-oriented design principles.

### Testability

Core business logic should be covered by deterministic unit tests.

### Performance

The application should efficiently handle approximately 10,000
employee records through:

- Pagination.
- Server-side filtering.
- Appropriate database indexes.
- Efficient database queries.

### Usability

The HR Manager should be able to quickly search, inspect, and analyze
salary information through a clear and understandable UI.

### Reliability

The system should provide appropriate validation and consistent
error handling.

---

## 8. Technology

### Backend

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- Maven
- SQLite
- JUnit 5
- Mockito

### Frontend

- Angular
- TypeScript
- RxJS

### Development

- Git
- Docker
- CI/CD

---

## 9. Architecture

The application will use a modular monolithic architecture:

Angular UI
    ↓
REST API
    ↓
Spring Boot
    ↓
Service Layer
    ↓
Repository Layer
    ↓
SQLite

A modular monolith is preferred over microservices because the
assessment does not require independently deployable services and
the problem domain can be handled effectively within a single
application.

---

## 10. Success Criteria

The solution is successful when:

- 10,000 employee records can be loaded.
- HR can search and filter employees.
- Salary information can be viewed.
- Salary records can be created and updated.
- Salary history can be viewed.
- Compensation analytics can be generated.
- Country and department comparisons are available.
- Core functionality is covered by tests.
- The application can be deployed and accessed.
- Architecture, trade-offs, assumptions, and AI usage are documented.