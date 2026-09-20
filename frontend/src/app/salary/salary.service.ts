import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SalaryRecordRequest, SalaryRecordResponse } from '../core/models/salary.model';
import { withoutErrorSnackbar } from '../core/http-error.interceptor';

@Injectable({ providedIn: 'root' })
export class SalaryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/employees`;

  /**
   * GET /api/employees/{employeeId}/salaries - newest first, per backend
   * ordering. Suppressed like the other two employee-scoped calls: if the
   * employeeId doesn't exist, this 404s too (same backend
   * EmployeeNotFoundException as getById), and the page's single
   * "Employee not found" state already covers it - a second toast with
   * the same message would be a confusing duplicate, not extra information.
   */
  getHistory(employeeId: number): Observable<SalaryRecordResponse[]> {
    return this.http.get<SalaryRecordResponse[]>(`${this.baseUrl}/${employeeId}/salaries`, {
      context: withoutErrorSnackbar()
    });
  }

  /**
   * GET /api/employees/{employeeId}/salary/current. A 404 (no salary
   * effective yet) is an expected, normal state - rendered inline as
   * "No current salary record", not a generic error snackbar.
   */
  getCurrentSalary(employeeId: number): Observable<SalaryRecordResponse> {
    return this.http.get<SalaryRecordResponse>(`${this.baseUrl}/${employeeId}/salary/current`, {
      context: withoutErrorSnackbar()
    });
  }

  /**
   * POST /api/employees/{employeeId}/salaries - adds a new history entry
   * (e.g. a raise). Errors (validation 400, employee-not-found 404,
   * duplicate-date 409) are NOT suppressed here: unlike the read-path
   * cases above, these are genuine anomalies during a user-initiated
   * write, and the backend's ApiError.message is already specific
   * (e.g. "A salary record already exists for employee 5 effective on
   * 2026-01-01") - the default snackbar is the right tool, not a
   * bespoke inline state.
   */
  create(employeeId: number, request: SalaryRecordRequest): Observable<SalaryRecordResponse> {
    return this.http.post<SalaryRecordResponse>(`${this.baseUrl}/${employeeId}/salaries`, request);
  }

  /**
   * PUT /api/employees/{employeeId}/salaries/{salaryId} - corrects an
   * existing record's fields in place. employeeId is only needed to build
   * the URL (the backend's own path shape); it does not verify ownership
   * server-side (documented backend limitation).
   */
  update(employeeId: number, salaryId: number, request: SalaryRecordRequest): Observable<SalaryRecordResponse> {
    return this.http.put<SalaryRecordResponse>(`${this.baseUrl}/${employeeId}/salaries/${salaryId}`, request);
  }
}
