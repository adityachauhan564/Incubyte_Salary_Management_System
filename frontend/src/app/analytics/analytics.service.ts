import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { GroupedSalaryStats, SalaryRangeBucket, SalarySummary } from '../core/models/analytics.model';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/analytics`;

  /** GET /api/analytics/summary */
  getSummary(): Observable<SalarySummary> {
    return this.http.get<SalarySummary>(`${this.baseUrl}/summary`);
  }

  /** GET /api/analytics/departments */
  getByDepartment(): Observable<GroupedSalaryStats[]> {
    return this.http.get<GroupedSalaryStats[]>(`${this.baseUrl}/departments`);
  }

  /** GET /api/analytics/countries */
  getByCountry(): Observable<GroupedSalaryStats[]> {
    return this.http.get<GroupedSalaryStats[]>(`${this.baseUrl}/countries`);
  }

  /** GET /api/analytics/distribution?bucketSize= */
  getDistribution(bucketSize: number): Observable<SalaryRangeBucket[]> {
    const params = new HttpParams().set('bucketSize', bucketSize);
    return this.http.get<SalaryRangeBucket[]>(`${this.baseUrl}/distribution`, { params });
  }
}
