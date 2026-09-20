# Salary Management System — Assumptions

This document records assumptions and scope decisions made where the
assessment requirements leave implementation details open.

These decisions are intentionally documented so that the implementation
remains transparent, consistent, and easy to discuss during the technical
review.

---

## 1. User and Authentication

### Assumption

The application assumes a single HR Manager user.

Authentication and authorization are not implemented.

### Reasoning

The assessment clarification explicitly states that authentication and
authorization are not required and that a single HR Manager user can be
assumed.

The application therefore focuses on salary management, employee information,
and compensation analysis rather than user management.

---

## 2. Employee Management

### Assumption

Employee management is primarily read-oriented.

The application will support:

- Employee listing
- Employee search
- Filtering
- Sorting
- Pagination
- Employee detail view

Full employee CRUD is not required.

Employee create/update functionality will only be introduced if it is
necessary to support the salary-management workflow.

Employee deletion is not implemented.

### Reasoning

The assessment clarification states that employee create/update functionality
may be included if useful, but a full employee management system is not
necessary.

Keeping employee management focused prevents unnecessary scope expansion.

---

## 3. Salary Management

### Assumption

Salary management will support:

- Viewing salary information
- Creating salary records
- Updating salary information

Salary deletion is not implemented.

### Reasoning

The assessment clarification explicitly states that view, create, and update
salary functionality is sufficient and that delete operations are not
required.

---

## 4. Salary History

### Assumption

An employee may have multiple salary records over time.

Each salary record will retain an effective date so that historical
compensation can be represented.

The current salary will be determined from the applicable salary record based
on the effective-date rules defined during implementation.

### Reasoning

Salary history is important for understanding how compensation has changed
over time.

Keeping historical salary records also avoids losing previous compensation
information when salary changes occur.

The exact update semantics will be finalized during database/domain design.

---

## 5. Compensation Analytics

### Assumption

The requirement to help an HR Manager understand "how the organization pays
people" will be implemented through structured filters, analytics, and
reports.

Natural-language queries are not required.

The initial analytics scope will include:

- Total headcount
- Total salary cost
- Average salary
- Median salary
- Minimum salary
- Maximum salary
- Salary distribution by salary ranges
- Salary comparison by department
- Salary comparison by country
- Salary cost by department
- Salary cost by country

### Reasoning

The assessment clarification explicitly states that natural-language queries
are not required and asks for a reasonable set of filters, analytics, or
reports.

The selected analytics provide HR-oriented views of salary cost,
distribution, and comparisons.

---

## 6. Currency Handling

### Assumption

Salary records will retain their original/native currency.

For cross-country comparisons and consolidated analytics, salaries may be
normalized to a common reporting currency.

### Reasoning

The assessment explicitly leaves the currency strategy open.

Retaining the native currency preserves the original salary information,
while normalization allows meaningful cross-country comparisons.

---

## 7. Exchange Rates

### Assumption

Currency conversion uses a documented static exchange-rate configuration
(`ExchangeRateConfig` in the analytics module). Live foreign-exchange data or
integration with an external FX provider is not required.

The reporting currency is **USD**. Rates are units of the given currency per
1 USD:

| Currency | Units per USD |
|----------|---------------|
| USD      | 1.00          |
| GBP      | 0.80          |
| EUR      | 0.90          |
| INR      | 83.00         |
| CAD      | 1.35          |
| AUD      | 1.50          |
| SGD      | 1.30          |
| BRL      | 5.30          |

These cover the currencies the seed dataset generates. A salary record
created via the API in a currency outside this table is rejected by
analytics with a 400 `UNSUPPORTED_CURRENCY` error rather than silently
mis-converted or excluded.

### Reasoning

The assessment does not require real-time currency conversion.

Static exchange rates provide deterministic and reproducible calculations,
which also makes automated testing easier.

---

## 8. Synthetic Employee Data

### Assumption

The application will be populated with approximately 10,000 synthetic
employee records.

The seed data should be deterministic where practical so that the same
dataset can be reproduced during development and demonstration.

### Reasoning

The assessment is intended to evaluate how the application handles a realistic
salary dataset rather than requiring integration with an existing HR system.

A reproducible dataset makes testing and demonstration more reliable.

---

## 9. Employee Data Fields

### Assumption

Employee records will contain the information necessary to support:

- Employee identification
- Department
- Country
- Salary association
- Employee search and filtering

Additional employee attributes will only be introduced when they support a
clear functional requirement.

### Reasoning

The assessment focuses on salary management and compensation analysis rather
than building a complete HR information system.

Avoiding unnecessary employee attributes keeps the domain model focused.

---

## 10. Pagination, Filtering and Sorting

### Assumption

Large employee and salary-related lists will use server-side pagination,
filtering, and sorting where applicable.

### Reasoning

The application is expected to handle approximately 10,000 employees.

Transferring the entire dataset to the browser would be unnecessary and would
not demonstrate appropriate handling of larger datasets.

Server-side operations also allow the database to perform filtering and
sorting efficiently.

---

## 11. Monetary Values

### Assumption

Monetary values will use an exact decimal representation rather than binary
floating-point arithmetic.

### Reasoning

Salary values represent financial amounts and should avoid floating-point
precision problems.

The exact Java and database representation will be finalized during database
design.

---

## 12. Analytics Precision and Rounding

### Assumption

Monetary analytics will use a consistent precision and rounding strategy.

The rounding rules will be documented and implemented consistently across
backend calculations and frontend presentation.

### Reasoning

Salary calculations and aggregated compensation values should be
deterministic and understandable.

---

## 13. Database

### Assumption

SQLite will be used as the relational database for the assessment.

Spring Data JPA and Hibernate will be used for persistence.

### Reasoning

The assessment dataset is approximately 10,000 synthetic employees and does
not require the operational complexity of a production-scale database
infrastructure.

SQLite provides a simple, self-contained database suitable for local
development, testing, and the assessment deployment.

---

## 14. API Design

### Assumption

The frontend and backend will communicate through REST APIs using JSON.

The API will expose only the operations required by the assessment scope.

Persistence entities will not be exposed directly as API responses; DTOs will
define the API contract.

### Reasoning

Separating API contracts from persistence models keeps the application
maintainable and prevents database implementation details from becoming part
of the frontend contract.

---

## 15. Error Handling

### Assumption

The backend will provide consistent error responses for:

- Invalid input
- Missing employees
- Missing salary records
- Invalid salary data
- Invalid query parameters
- Unexpected application errors

Internal implementation details and stack traces will not be exposed to the
client.

### Reasoning

Consistent error handling makes the API easier for the Angular frontend to
consume and improves maintainability.

---

## 16. Deployment

### Assumption

No specific deployment provider is required.

The application will be deployed to a suitable platform that allows the
reviewers to access and evaluate the fully functional application.

### Reasoning

The assessment explicitly states that there is no preferred deployment
platform.

The deployment approach will therefore prioritize:

- Accessibility for reviewers
- Reliability
- Simplicity of deployment
- Compatibility with the application architecture

## 17. Authentication and Security Scope

### Assumption

The following are outside the assessment scope:

- User registration
- Login
- Password management
- Role management
- Authentication
- Authorization

Basic secure development practices will still be followed, including input
validation, safe error responses, and avoiding sensitive information in logs.

### Reasoning

Authentication and authorization were explicitly excluded from the
assessment requirements.

---

## 18. Out-of-Scope Features

The following features will not be implemented unless the requirements
change:

- Employee deletion
- Full employee CRUD
- Salary deletion
- Authentication
- Authorization
- Natural-language salary queries
- Payroll processing
- Tax calculation
- Employee self-service
- Real-time FX integration
- Unnecessary distributed infrastructure
- Features unrelated to salary management and compensation analysis

### Reasoning

These features either have been explicitly excluded by the assessment
clarifications or would expand the solution beyond the intended scope.

The goal is to demonstrate engineering judgment by delivering the required
functionality rather than maximizing the number of features.