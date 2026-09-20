import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { Subject, of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { EmployeeDetailComponent } from './employee-detail.component';
import { EmployeeService } from '../employee.service';
import { SalaryService } from '../../salary/salary.service';
import { EmployeeResponse } from '../../core/models/employee.model';
import { SalaryRecordResponse } from '../../core/models/salary.model';

describe('EmployeeDetailComponent', () => {
  let fixture: ComponentFixture<EmployeeDetailComponent>;
  let component: EmployeeDetailComponent;
  let employeeService: jasmine.SpyObj<EmployeeService>;
  let salaryService: jasmine.SpyObj<SalaryService>;
  let dialog: jasmine.SpyObj<MatDialog>;

  const employee: EmployeeResponse = {
    id: 1,
    firstName: 'Ada',
    lastName: 'Lovelace',
    country: 'United Kingdom',
    department: 'Engineering',
    jobTitle: 'Software Engineer'
  };

  const currentSalary: SalaryRecordResponse = {
    id: 10,
    amount: 95000,
    currency: 'USD',
    effectiveDate: '2026-01-01',
    createdAt: '2026-09-20T00:00:00Z'
  };

  const notFound = new HttpErrorResponse({ status: 404, statusText: 'Not Found' });

  beforeEach(async () => {
    employeeService = jasmine.createSpyObj('EmployeeService', ['getById']);
    salaryService = jasmine.createSpyObj('SalaryService', ['getCurrentSalary', 'getHistory']);
    dialog = jasmine.createSpyObj('MatDialog', ['open']);

    await TestBed.configureTestingModule({
      imports: [EmployeeDetailComponent],
      providers: [
        provideRouter([]),
        { provide: EmployeeService, useValue: employeeService },
        { provide: SalaryService, useValue: salaryService },
        { provide: MatDialog, useValue: dialog }
      ]
    }).compileComponents();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(EmployeeDetailComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('id', '1');
  }

  it('renders employee info, current salary, and history on success', () => {
    employeeService.getById.and.returnValue(of(employee));
    salaryService.getCurrentSalary.and.returnValue(of(currentSalary));
    salaryService.getHistory.and.returnValue(of([currentSalary]));

    createComponent();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Ada Lovelace');
    expect(text).toContain('95,000.00');
    expect(text).toContain('USD');
    expect(employeeService.getById).toHaveBeenCalledWith(1);
  });

  it('renders "No current salary record" instead of an error when current salary is 404', () => {
    employeeService.getById.and.returnValue(of(employee));
    salaryService.getCurrentSalary.and.returnValue(throwError(() => notFound));
    salaryService.getHistory.and.returnValue(of([]));

    createComponent();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No current salary record.');
  });

  it('renders "Employee not found" when the employee id does not exist', () => {
    employeeService.getById.and.returnValue(throwError(() => notFound));
    salaryService.getCurrentSalary.and.returnValue(throwError(() => notFound));
    salaryService.getHistory.and.returnValue(of([]));

    createComponent();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Employee not found.');
  });

  it('shows a loading spinner for the employee section while the request is in flight', () => {
    const subject = new Subject<EmployeeResponse>();
    employeeService.getById.and.returnValue(subject.asObservable());
    salaryService.getCurrentSalary.and.returnValue(of(currentSalary));
    salaryService.getHistory.and.returnValue(of([]));

    createComponent();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('mat-spinner')).toBeTruthy();

    subject.next(employee);
    subject.complete();
  });

  it('opens the salary form in create mode and refreshes salary data when a record is created', () => {
    employeeService.getById.and.returnValue(of(employee));
    salaryService.getCurrentSalary.and.returnValue(of(currentSalary));
    salaryService.getHistory.and.returnValue(of([currentSalary]));
    const afterClosed = new Subject<SalaryRecordResponse | undefined>();
    dialog.open.and.returnValue({ afterClosed: () => afterClosed.asObservable() } as any);

    createComponent();
    fixture.detectChanges();

    component.openCreateDialog();

    expect(dialog.open).toHaveBeenCalledWith(jasmine.any(Function), {
      data: { employeeId: 1, mode: 'create' }
    });

    salaryService.getCurrentSalary.calls.reset();
    salaryService.getHistory.calls.reset();
    afterClosed.next(currentSalary);

    expect(salaryService.getCurrentSalary).toHaveBeenCalledWith(1);
    expect(salaryService.getHistory).toHaveBeenCalledWith(1);
  });

  it('does not refresh salary data when the create dialog is cancelled', () => {
    employeeService.getById.and.returnValue(of(employee));
    salaryService.getCurrentSalary.and.returnValue(of(currentSalary));
    salaryService.getHistory.and.returnValue(of([currentSalary]));
    const afterClosed = new Subject<SalaryRecordResponse | undefined>();
    dialog.open.and.returnValue({ afterClosed: () => afterClosed.asObservable() } as any);

    createComponent();
    fixture.detectChanges();

    component.openCreateDialog();
    salaryService.getCurrentSalary.calls.reset();
    salaryService.getHistory.calls.reset();
    afterClosed.next(undefined);

    expect(salaryService.getCurrentSalary).not.toHaveBeenCalled();
    expect(salaryService.getHistory).not.toHaveBeenCalled();
  });

  it('opens the salary form in update mode with the selected record', () => {
    employeeService.getById.and.returnValue(of(employee));
    salaryService.getCurrentSalary.and.returnValue(of(currentSalary));
    salaryService.getHistory.and.returnValue(of([currentSalary]));
    dialog.open.and.returnValue({ afterClosed: () => new Subject().asObservable() } as any);

    createComponent();
    fixture.detectChanges();

    component.openCorrectDialog(currentSalary);

    expect(dialog.open).toHaveBeenCalledWith(jasmine.any(Function), {
      data: { employeeId: 1, mode: 'update', record: currentSalary }
    });
  });
});
