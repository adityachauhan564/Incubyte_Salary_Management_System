import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { AnalyticsService } from './analytics.service';
import { GroupedSalaryStats, SalaryRangeBucket, SalarySummary } from '../core/models/analytics.model';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let httpMock: HttpTestingController;

  const summary: SalarySummary = {
    totalEmployees: 10000,
    totalSalaryCost: 950000000,
    averageSalary: 95000,
    medianSalary: 92000,
    minSalary: 42000,
    maxSalary: 210000,
    currency: 'USD'
  };

  const groupedStats: GroupedSalaryStats[] = [
    { name: 'Engineering', headcount: 1200, totalCost: 114000000, averageSalary: 95000, minSalary: 60000, maxSalary: 210000, currency: 'USD' }
  ];

  const buckets: SalaryRangeBucket[] = [
    { label: '50000.00 - 75000.00', rangeStart: 50000, rangeEnd: 75000, count: 1450, currency: 'USD' }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AnalyticsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getSummary() calls GET /api/analytics/summary', () => {
    service.getSummary().subscribe((result) => expect(result).toEqual(summary));

    const req = httpMock.expectOne('/api/analytics/summary');
    expect(req.request.method).toBe('GET');
    req.flush(summary);
  });

  it('getByDepartment() calls GET /api/analytics/departments', () => {
    service.getByDepartment().subscribe((result) => expect(result).toEqual(groupedStats));

    const req = httpMock.expectOne('/api/analytics/departments');
    expect(req.request.method).toBe('GET');
    req.flush(groupedStats);
  });

  it('getByCountry() calls GET /api/analytics/countries', () => {
    service.getByCountry().subscribe((result) => expect(result).toEqual(groupedStats));

    const req = httpMock.expectOne('/api/analytics/countries');
    expect(req.request.method).toBe('GET');
    req.flush(groupedStats);
  });

  it('getDistribution() calls GET /api/analytics/distribution with bucketSize', () => {
    service.getDistribution(25000).subscribe((result) => expect(result).toEqual(buckets));

    const req = httpMock.expectOne('/api/analytics/distribution?bucketSize=25000');
    expect(req.request.method).toBe('GET');
    req.flush(buckets);
  });
});
