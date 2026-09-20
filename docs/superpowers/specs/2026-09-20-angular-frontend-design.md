# Angular Frontend — Design

## Goal

Build the Angular UI for the Salary Management System against the existing,
fully-built and documented backend API (see `docs/architecture.md` and the
live Swagger UI at `/swagger-ui/index.html`). No backend changes are in
scope. The frontend must let an HR Manager do everything the API already
supports: search/view employees, view/create/correct salary records, and
view compensation analytics.

## Decisions

| Area | Decision | Why |
|---|---|---|
| Component library | Angular Material | Official, standalone-component friendly, covers tables/forms/dialogs/pagination out of the box |
| State management | Angular Signals + plain services | Built-in, no extra dependency; proportional to a handful of CRUD screens + dashboards, not complex enough to justify NgRx |
| Charts | ngx-charts | Angular-native (components, not a canvas wrapper), covers bar charts and histograms |
| Folder structure | Feature-based (`employees/`, `salary/`, `analytics/`, `core/`, `shared/`) | Mirrors the backend's own package-by-feature convention for consistency |

## Architecture

```
frontend/src/app/
├── core/
│   ├── models/                     # TS interfaces mirroring backend DTOs exactly
│   │   ├── employee.model.ts       # EmployeeResponse, EmployeeSearchCriteria
│   │   ├── salary.model.ts         # SalaryRecordResponse, SalaryRecordRequest
│   │   ├── analytics.model.ts      # SalarySummary, GroupedSalaryStats, SalaryRangeBucket
│   │   ├── page.model.ts           # PagedModel<T> shape (content + page metadata)
│   │   └── api-error.model.ts      # ApiError
│   └── http-error.interceptor.ts   # catches ApiError responses, surfaces via snackbar
├── employees/
│   ├── employee.service.ts
│   ├── employee-list/
│   └── employee-detail/
├── salary/
│   ├── salary.service.ts
│   └── salary-form/                # shared dialog: used for both create and correct
├── analytics/
│   ├── analytics.service.ts
│   └── analytics-dashboard/
├── shared/                          # loading spinner, empty-state, error banner
├── app.routes.ts
├── app.config.ts
└── environments/
    └── environment.ts               # apiBaseUrl
```

Each `*.service.ts` is a thin `HttpClient` wrapper over exactly the
endpoints already documented in Swagger — one service per backend module,
matching the backend's own module boundaries. No service calls an endpoint
that doesn't already exist.

## Screens

### Employee List (`/employees`)
- Material table with columns: name, country, department, job title.
- `mat-paginator` bound to the backend's `page`/`size` query params — never
  fetches more than one page at a time, regardless of total employee count.
- Search box (name) + country/department/job-title filter selects, all
  optional, combining exactly as `EmployeeSpecifications` already does
  server-side.
- Row click navigates to `/employees/:id`.

### Employee Detail (`/employees/:id`)
- Employee info card (name, country, department, job title).
- Current salary card — calls `GET .../salary/current`; if 404 (no salary
  yet), shows an empty state instead of an error.
- Salary history table — calls `GET .../salaries`, newest first (matches
  backend ordering).
- "Add Salary Record" button opens the shared salary form dialog in
  *create* mode (`POST`).
- Each history row has a "Correct" action opening the same dialog in
  *update* mode (`PUT`), pre-filled with that record's current values.

### Analytics Dashboard (`/analytics`)
- Stat cards: headcount, total cost, average, median, min, max (from
  `GET /api/analytics/summary`).
- Bar chart: department comparison (cost + average) from `/departments`.
- Bar chart: country comparison from `/countries`.
- Histogram: salary distribution from `/distribution`, with a bucket-size
  input control that re-fetches on change.

## API error handling

One `HttpInterceptorFn` catches every non-2xx response, reads the body as
`ApiError` (the shape is already fixed and documented backend-side), and
surfaces `message` via a Material snackbar. Components don't each implement
their own error handling — this is the one place it happens, mirroring the
backend's own single `GlobalExceptionHandler`.

## Build order

1. **Core setup** — install Material + ngx-charts, routing shell + nav,
   TS models, HTTP error interceptor, `environment.ts`.
2. **Read path** — Employee List + Employee Detail (read-only: info,
   current salary, history). Verified end-to-end against the running
   backend before moving on.
3. **Write path** — salary create/correct forms wired into Employee Detail.
4. **Analytics** — stat cards first, then the two comparison charts, then
   the distribution histogram.
5. **Polish** — loading states, empty states (no salary yet, no search
   results), a responsive layout pass at typical laptop widths.

Each step is verified by actually running `ng serve` against the real
backend (not just unit tests) before moving to the next, per this
project's established practice of manually confirming UI behavior.

## Explicitly out of scope

- No new backend endpoints or backend changes of any kind.
- No authentication (matches the backend's own non-goal).
- No state-management library (NgRx) — signals are sufficient at this
  scope; revisit only if cross-cutting state actually becomes unmanageable.
- No mobile-first redesign — this is an internal HR tool; the responsive
  pass targets "doesn't break at typical laptop widths," not phone layouts.

## Testing

Component/service tests using whatever the Angular CLI scaffold already
set up (confirmed at implementation time, not assumed here) — focused on
the same things the backend tests focus on: does the service call the
right endpoint with the right params, does the component render the right
state for loading/empty/error/success.
