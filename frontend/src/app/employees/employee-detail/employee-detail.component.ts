import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';

import { EmployeeService } from '../employee.service';
import { SalaryService } from '../../salary/salary.service';
import { EmployeeResponse } from '../../core/models/employee.model';
import { SalaryRecordResponse } from '../../core/models/salary.model';
import { SalaryFormComponent, SalaryFormDialogData } from '../../salary/salary-form/salary-form.component';

@Component({
  selector: 'app-employee-detail',
  imports: [
    RouterLink,
    DecimalPipe,
    DatePipe,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './employee-detail.component.html',
  styleUrl: './employee-detail.component.scss'
})
export class EmployeeDetailComponent {
  private readonly employeeService = inject(EmployeeService);
  private readonly salaryService = inject(SalaryService);
  private readonly dialog = inject(MatDialog);

  // Bound directly from the /employees/:id route param (withComponentInputBinding).
  readonly id = input.required<string>();

  readonly employee = signal<EmployeeResponse | null>(null);
  readonly employeeNotFound = signal(false);
  readonly loadingEmployee = signal(false);

  readonly currentSalary = signal<SalaryRecordResponse | null>(null);
  readonly hasCurrentSalary = signal(false);
  readonly loadingCurrentSalary = signal(false);

  readonly history = signal<SalaryRecordResponse[]>([]);
  readonly loadingHistory = signal(false);

  readonly historyColumns = ['effectiveDate', 'amount', 'currency', 'createdAt', 'actions'];

  constructor() {
    effect(() => {
      const employeeId = Number(this.id());
      this.loadEmployee(employeeId);
      this.loadCurrentSalary(employeeId);
      this.loadHistory(employeeId);
    });
  }

  private loadEmployee(employeeId: number): void {
    this.loadingEmployee.set(true);
    this.employeeNotFound.set(false);
    this.employeeService.getById(employeeId).subscribe({
      next: (employee) => {
        this.employee.set(employee);
        this.loadingEmployee.set(false);
      },
      error: () => {
        this.employee.set(null);
        this.employeeNotFound.set(true);
        this.loadingEmployee.set(false);
      }
    });
  }

  private loadCurrentSalary(employeeId: number): void {
    this.loadingCurrentSalary.set(true);
    this.hasCurrentSalary.set(false);
    this.salaryService.getCurrentSalary(employeeId).subscribe({
      next: (salary) => {
        this.currentSalary.set(salary);
        this.hasCurrentSalary.set(true);
        this.loadingCurrentSalary.set(false);
      },
      error: () => {
        // 404 here just means "no current salary yet" - not an app error.
        this.currentSalary.set(null);
        this.hasCurrentSalary.set(false);
        this.loadingCurrentSalary.set(false);
      }
    });
  }

  private loadHistory(employeeId: number): void {
    this.loadingHistory.set(true);
    this.salaryService.getHistory(employeeId).subscribe({
      next: (records) => {
        this.history.set(records);
        this.loadingHistory.set(false);
      },
      error: () => {
        this.history.set([]);
        this.loadingHistory.set(false);
      }
    });
  }

  openCreateDialog(): void {
    this.openSalaryForm({ employeeId: Number(this.id()), mode: 'create' });
  }

  openCorrectDialog(record: SalaryRecordResponse): void {
    this.openSalaryForm({ employeeId: Number(this.id()), mode: 'update', record });
  }

  private openSalaryForm(data: SalaryFormDialogData): void {
    this.dialog
      .open(SalaryFormComponent, { data })
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          // A record was created/corrected - re-fetch so current salary and
          // history reflect it. Employee info itself never changes here.
          const employeeId = Number(this.id());
          this.loadCurrentSalary(employeeId);
          this.loadHistory(employeeId);
        }
      });
  }
}
