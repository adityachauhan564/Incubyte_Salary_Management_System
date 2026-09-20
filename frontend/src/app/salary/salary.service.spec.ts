import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { SalaryService } from './salary.service';
import { SalaryRecordRequest, SalaryRecordResponse } from '../core/models/salary.model';

describe('SalaryService', () => {
  let service: SalaryService;
  let httpMock: HttpTestingController;

  const record: SalaryRecordResponse = {
    id: 10,
    amount: 95000,
    currency: 'USD',
    effectiveDate: '2026-01-01',
    createdAt: '2026-09-20T00:00:00Z'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(SalaryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getHistory() calls GET /api/employees/{employeeId}/salaries', () => {
    service.getHistory(7).subscribe((result) => expect(result).toEqual([record]));

    const req = httpMock.expectOne('/api/employees/7/salaries');
    expect(req.request.method).toBe('GET');
    req.flush([record]);
  });

  it('getCurrentSalary() calls GET /api/employees/{employeeId}/salary/current', () => {
    service.getCurrentSalary(7).subscribe((result) => expect(result).toEqual(record));

    const req = httpMock.expectOne('/api/employees/7/salary/current');
    expect(req.request.method).toBe('GET');
    req.flush(record);
  });

  it('create() calls POST /api/employees/{employeeId}/salaries with the request body', () => {
    const request: SalaryRecordRequest = { amount: 95000, currency: 'USD', effectiveDate: '2026-01-01' };
    service.create(7, request).subscribe((result) => expect(result).toEqual(record));

    const req = httpMock.expectOne('/api/employees/7/salaries');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(record);
  });

  it('update() calls PUT /api/employees/{employeeId}/salaries/{salaryId} with the request body', () => {
    const request: SalaryRecordRequest = { amount: 97000, currency: 'USD', effectiveDate: '2026-02-01' };
    service.update(7, 10, request).subscribe((result) => expect(result).toEqual(record));

    const req = httpMock.expectOne('/api/employees/7/salaries/10');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(record);
  });
});
