import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { SalaryService } from '../salary.service';
import { SalaryRecordRequest, SalaryRecordResponse } from '../../core/models/salary.model';
import { parseIsoDate, toIsoDate } from '../iso-date.util';

export interface SalaryFormDialogData {
  employeeId: number;
  mode: 'create' | 'update';
  /** Required when mode === 'update'; the record being corrected. */
  record?: SalaryRecordResponse;
}

@Component({
  selector: 'app-salary-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './salary-form.component.html',
  styleUrl: './salary-form.component.scss'
})
export class SalaryFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly salaryService = inject(SalaryService);
  private readonly dialogRef = inject(MatDialogRef<SalaryFormComponent, SalaryRecordResponse | undefined>);
  readonly data = inject<SalaryFormDialogData>(MAT_DIALOG_DATA);

  readonly submitting = signal(false);
  readonly isUpdate = this.data.mode === 'update';

  readonly form = this.fb.nonNullable.group({
    amount: this.fb.control<number | null>(this.data.record?.amount ?? null, [
      Validators.required,
      Validators.min(0.01)
    ]),
    currency: [this.data.record?.currency ?? '', [Validators.required, Validators.pattern(/^[A-Z]{3}$/)]],
    effectiveDate: this.fb.control<Date | null>(
      this.data.record ? parseIsoDate(this.data.record.effectiveDate) : null,
      Validators.required
    )
  });

  constructor() {
    // Live-uppercase so what the user sees always matches the backend's
    // required pattern (^[A-Z]{3}$) instead of flashing invalid while typing.
    this.form.controls.currency.valueChanges.subscribe((value) => {
      const upper = value.toUpperCase();
      if (upper !== value) {
        this.form.controls.currency.setValue(upper, { emitEvent: false });
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request: SalaryRecordRequest = {
      amount: value.amount!,
      currency: value.currency,
      effectiveDate: toIsoDate(value.effectiveDate!)
    };

    this.submitting.set(true);
    const call = this.isUpdate
      ? this.salaryService.update(this.data.employeeId, this.data.record!.id, request)
      : this.salaryService.create(this.data.employeeId, request);

    call.subscribe({
      next: (result) => {
        this.submitting.set(false);
        this.dialogRef.close(result);
      },
      error: () => {
        // Backend's specific message is already shown by the global
        // interceptor's snackbar; keep the dialog open with the entered
        // values so the user can correct and resubmit.
        this.submitting.set(false);
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(undefined);
  }
}
