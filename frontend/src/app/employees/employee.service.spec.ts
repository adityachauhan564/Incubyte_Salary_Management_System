import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { EmployeeService } from './employee.service';
import { EmployeeResponse, EmployeeSearchCriteria } from '../core/models/employee.model';
import { PagedModel } from '../core/models/page.model';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(EmployeeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('search() calls GET /api/employees with page/size and only the non-empty filters', () => {
    const criteria: EmployeeSearchCriteria = { search: 'Ada', country: 'United Kingdom' };
    const expectedPage: PagedModel<EmployeeResponse> = {
      content: [],
      page: { size: 20, number: 0, totalElements: 0, totalPages: 0 }
    };

    service.search(criteria, 0, 20).subscribe((result) => expect(result).toEqual(expectedPage));

    const req = httpMock.expectOne(
      (r) => r.url === '/api/employees' && r.method === 'GET'
    );
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.get('search')).toBe('Ada');
    expect(req.request.params.get('country')).toBe('United Kingdom');
    expect(req.request.params.has('department')).toBeFalse();
    expect(req.request.params.has('jobTitle')).toBeFalse();
    req.flush(expectedPage);
  });

  it('getById() calls GET /api/employees/{id}', () => {
    const expected: EmployeeResponse = {
      id: 7,
      firstName: 'Ada',
      lastName: 'Lovelace',
      country: 'United Kingdom',
      department: 'Engineering',
      jobTitle: 'Software Engineer'
    };

    service.getById(7).subscribe((result) => expect(result).toEqual(expected));

    const req = httpMock.expectOne('/api/employees/7');
    expect(req.request.method).toBe('GET');
    req.flush(expected);
  });
});
