import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { EmployeeResponse, EmployeeSearchCriteria } from '../core/models/employee.model';
import { PagedModel } from '../core/models/page.model';
import { withoutErrorSnackbar } from '../core/http-error.interceptor';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/employees`;

  /** GET /api/employees - matches EmployeeController.search() exactly. */
  search(criteria: EmployeeSearchCriteria, page: number, size: number): Observable<PagedModel<EmployeeResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (criteria.search) {
      params = params.set('search', criteria.search);
    }
    if (criteria.country) {
      params = params.set('country', criteria.country);
    }
    if (criteria.department) {
      params = params.set('department', criteria.department);
    }
    if (criteria.jobTitle) {
      params = params.set('jobTitle', criteria.jobTitle);
    }
    return this.http.get<PagedModel<EmployeeResponse>>(this.baseUrl, { params });
  }

  /**
   * GET /api/employees/{id} - matches EmployeeController.getById() exactly.
   * A 404 here (bad id) is rendered as an inline "not found" state by the
   * detail page, not a generic error snackbar - suppressed at the source.
   */
  getById(id: number): Observable<EmployeeResponse> {
    return this.http.get<EmployeeResponse>(`${this.baseUrl}/${id}`, { context: withoutErrorSnackbar() });
  }
}
