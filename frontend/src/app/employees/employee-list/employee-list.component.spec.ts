import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject, of } from 'rxjs';

import { EmployeeListComponent } from './employee-list.component';
import { EmployeeService } from '../employee.service';
import { EmployeeResponse } from '../../core/models/employee.model';
import { PagedModel } from '../../core/models/page.model';

describe('EmployeeListComponent', () => {
  let component: EmployeeListComponent;
  let fixture: ComponentFixture<EmployeeListComponent>;
  let employeeService: jasmine.SpyObj<EmployeeService>;

  const employee: EmployeeResponse = {
    id: 1,
    firstName: 'Ada',
    lastName: 'Lovelace',
    country: 'United Kingdom',
    department: 'Engineering',
    jobTitle: 'Software Engineer'
  };

  function pageOf(content: EmployeeResponse[]): PagedModel<EmployeeResponse> {
    return { content, page: { size: 20, number: 0, totalElements: content.length, totalPages: 1 } };
  }

  beforeEach(async () => {
    employeeService = jasmine.createSpyObj('EmployeeService', ['search']);

    await TestBed.configureTestingModule({
      imports: [EmployeeListComponent],
      providers: [provideRouter([]), { provide: EmployeeService, useValue: employeeService }]
    }).compileComponents();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(EmployeeListComponent);
    component = fixture.componentInstance;
  }

  it('renders a loading spinner while the search request is in flight', () => {
    const subject = new Subject<PagedModel<EmployeeResponse>>();
    employeeService.search.and.returnValue(subject.asObservable());

    createComponent();
    fixture.detectChanges();

    expect(component.loading()).toBeTrue();
    expect(fixture.nativeElement.querySelector('mat-spinner')).toBeTruthy();

    subject.next(pageOf([]));
    subject.complete();
  });

  it('renders an empty state when no employees match', () => {
    employeeService.search.and.returnValue(of(pageOf([])));

    createComponent();
    fixture.detectChanges();

    expect(component.loading()).toBeFalse();
    expect(fixture.nativeElement.textContent).toContain('No employees match your search.');
  });

  it('renders the employee table when the search succeeds', () => {
    employeeService.search.and.returnValue(of(pageOf([employee])));

    createComponent();
    fixture.detectChanges();

    expect(component.employees()).toEqual([employee]);
    expect(fixture.nativeElement.textContent).toContain('Ada Lovelace');
    expect(fixture.nativeElement.textContent).toContain('United Kingdom');
  });

  it('sends only the filters that were actually filled in', () => {
    employeeService.search.and.returnValue(of(pageOf([])));
    createComponent();
    fixture.detectChanges();

    component.country = 'France';
    component.applyFilters();

    expect(employeeService.search).toHaveBeenCalledWith(
      { search: undefined, country: 'France', department: undefined, jobTitle: undefined },
      0,
      20
    );
  });
});
