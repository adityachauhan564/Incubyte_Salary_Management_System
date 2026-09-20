import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideNativeDateAdapter } from '@angular/material/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Subject, throwError } from 'rxjs';

import { SalaryFormComponent, SalaryFormDialogData } from './salary-form.component';
import { SalaryService } from '../salary.service';
import { SalaryRecordResponse } from '../../core/models/salary.model';

describe('SalaryFormComponent', () => {
  let fixture: ComponentFixture<SalaryFormComponent>;
  let component: SalaryFormComponent;
  let salaryService: jasmine.SpyObj<SalaryService>;
  let dialogRef: jasmine.SpyObj<MatDialogRef<SalaryFormComponent>>;

  const existingRecord: SalaryRecordResponse = {
    id: 10,
    amount: 90000,
    currency: 'GBP',
    effectiveDate: '2026-01-01',
    createdAt: '2026-09-20T00:00:00Z'
  };

  const created: SalaryRecordResponse = {
    id: 11,
    amount: 95000,
    currency: 'USD',
    effectiveDate: '2026-02-01',
    createdAt: '2026-09-20T00:00:00Z'
  };

  function setup(data: SalaryFormDialogData): void {
    salaryService = jasmine.createSpyObj('SalaryService', ['create', 'update']);
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

    TestBed.configureTestingModule({
      imports: [SalaryFormComponent, NoopAnimationsModule],
      providers: [
        { provide: SalaryService, useValue: salaryService },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
        provideNativeDateAdapter()
      ]
    });

    fixture = TestBed.createComponent(SalaryFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('create mode starts with an empty, invalid form', () => {
    setup({ employeeId: 7, mode: 'create' });

    expect(component.form.valid).toBeFalse();
    expect(component.isUpdate).toBeFalse();
  });

  it('update mode pre-fills the form from the existing record', () => {
    setup({ employeeId: 7, mode: 'update', record: existingRecord });

    expect(component.form.valid).toBeTrue();
    expect(component.form.controls.amount.value).toBe(90000);
    expect(component.form.controls.currency.value).toBe('GBP');
    expect(component.form.controls.effectiveDate.value).toEqual(new Date(2026, 0, 1));
  });

  it('rejects a non-positive amount', () => {
    setup({ employeeId: 7, mode: 'create' });
    component.form.controls.amount.setValue(0);
    expect(component.form.controls.amount.invalid).toBeTrue();
  });

  it('rejects a currency that is not a 3-letter code, but auto-uppercases valid input', () => {
    setup({ employeeId: 7, mode: 'create' });

    component.form.controls.currency.setValue('us');
    expect(component.form.controls.currency.value).toBe('US');
    expect(component.form.controls.currency.invalid).toBeTrue();

    component.form.controls.currency.setValue('usd');
    expect(component.form.controls.currency.value).toBe('USD');
    expect(component.form.controls.currency.valid).toBeTrue();
  });

  it('does not call the service and marks fields touched when submitting an invalid form', () => {
    setup({ employeeId: 7, mode: 'create' });

    component.submit();

    expect(salaryService.create).not.toHaveBeenCalled();
    expect(component.form.controls.amount.touched).toBeTrue();
  });

  it('create mode calls SalaryService.create() with an ISO date string and closes the dialog on success', () => {
    setup({ employeeId: 7, mode: 'create' });
    salaryService.create.and.returnValue(new Subject<SalaryRecordResponse>().asObservable());

    component.form.setValue({ amount: 95000, currency: 'usd', effectiveDate: new Date(2026, 1, 1) });
    component.submit();

    expect(salaryService.create).toHaveBeenCalledWith(7, {
      amount: 95000,
      currency: 'USD',
      effectiveDate: '2026-02-01'
    });
  });

  it('update mode calls SalaryService.update() with the employeeId and salaryId', () => {
    setup({ employeeId: 7, mode: 'update', record: existingRecord });
    salaryService.update.and.returnValue(new Subject<SalaryRecordResponse>().asObservable());

    component.form.controls.amount.setValue(92000);
    component.submit();

    expect(salaryService.update).toHaveBeenCalledWith(7, 10, {
      amount: 92000,
      currency: 'GBP',
      effectiveDate: '2026-01-01'
    });
  });

  it('closes the dialog with the result on success', () => {
    setup({ employeeId: 7, mode: 'create' });
    const subject = new Subject<SalaryRecordResponse>();
    salaryService.create.and.returnValue(subject.asObservable());

    component.form.setValue({ amount: 95000, currency: 'USD', effectiveDate: new Date(2026, 1, 1) });
    component.submit();
    expect(component.submitting()).toBeTrue();

    subject.next(created);
    subject.complete();

    expect(dialogRef.close).toHaveBeenCalledWith(created);
    expect(component.submitting()).toBeFalse();
  });

  it('keeps the dialog open and re-enables the form when the backend rejects the submission', () => {
    setup({ employeeId: 7, mode: 'create' });
    salaryService.create.and.returnValue(throwError(() => new Error('409 conflict')));

    component.form.setValue({ amount: 95000, currency: 'USD', effectiveDate: new Date(2026, 1, 1) });
    component.submit();

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component.submitting()).toBeFalse();
  });

  it('cancel() closes the dialog with no result', () => {
    setup({ employeeId: 7, mode: 'create' });
    component.cancel();
    expect(dialogRef.close).toHaveBeenCalledWith(undefined);
  });
});
