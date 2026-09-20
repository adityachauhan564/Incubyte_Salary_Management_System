import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    loadComponent: () =>
      import('./employees/employee-list/employee-list.component').then((m) => m.EmployeeListComponent)
  },
  {
    path: 'employees/:id',
    loadComponent: () =>
      import('./employees/employee-detail/employee-detail.component').then((m) => m.EmployeeDetailComponent)
  },
  {
    path: 'analytics',
    loadComponent: () =>
      import('./analytics/analytics-dashboard/analytics-dashboard.component').then(
        (m) => m.AnalyticsDashboardComponent
      )
  },
  { path: '**', redirectTo: 'employees' }
];
