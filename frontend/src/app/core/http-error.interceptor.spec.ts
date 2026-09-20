import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';

import { httpErrorInterceptor, withoutErrorSnackbar } from './http-error.interceptor';
import { ApiError } from './models/api-error.model';

describe('httpErrorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const apiError: ApiError = {
    timestamp: '2026-09-20T00:00:00Z',
    status: 404,
    error: 'EMPLOYEE_NOT_FOUND',
    message: 'Employee not found: 999',
    path: '/api/employees/999'
  };

  beforeEach(() => {
    const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([httpErrorInterceptor])),
        provideHttpClientTesting(),
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    snackBar = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;
  });

  afterEach(() => httpMock.verify());

  it('shows the ApiError message via snackbar for a normal request', () => {
    http.get('/api/employees/999').subscribe({ error: () => {} });

    httpMock.expectOne('/api/employees/999').flush(apiError, { status: 404, statusText: 'Not Found' });

    expect(snackBar.open).toHaveBeenCalledWith('Employee not found: 999', 'Dismiss', { duration: 5000 });
  });

  it('does not show a snackbar when the request opts out via withoutErrorSnackbar()', () => {
    http.get('/api/employees/999/salary/current', { context: withoutErrorSnackbar() }).subscribe({ error: () => {} });

    httpMock
      .expectOne('/api/employees/999/salary/current')
      .flush(apiError, { status: 404, statusText: 'Not Found' });

    expect(snackBar.open).not.toHaveBeenCalled();
  });

  it('still propagates the error to the caller even when the snackbar is suppressed', () => {
    let caughtStatus: number | undefined;

    http
      .get('/api/employees/999/salary/current', { context: withoutErrorSnackbar() })
      .subscribe({ error: (err) => (caughtStatus = err.status) });

    httpMock
      .expectOne('/api/employees/999/salary/current')
      .flush(apiError, { status: 404, statusText: 'Not Found' });

    expect(caughtStatus).toBe(404);
  });
});
