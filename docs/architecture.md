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