import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { EmployeeService } from '../employee.service';
import { EmployeeResponse, EmployeeSearchCriteria } from '../../core/models/employee.model';

@Component({
  selector: 'app-employee-list',
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.scss'
})
export class EmployeeListComponent {
  private readonly employeeService = inject(EmployeeService);
  private readonly router = inject(Router);

  readonly displayedColumns = ['name', 'country', 'department', 'jobTitle'];

  readonly employees = signal<EmployeeResponse[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly loadFailed = signal(false);

  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  // Bound to the filter inputs via ngModel; applied on "Search".
  searchTerm = '';
  country = '';
  department = '';
  jobTitle = '';

  private appliedCriteria: EmployeeSearchCriteria = {};

  constructor() {
    this.load();
  }

  applyFilters(): void {
    this.appliedCriteria = {
      search: this.searchTerm.trim() || undefined,
      country: this.country.trim() || undefined,
      department: this.department.trim() || undefined,
      jobTitle: this.jobTitle.trim() || undefined
    };
    this.pageIndex.set(0);
    this.load();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.country = '';
    this.department = '';
    this.jobTitle = '';
    this.appliedCriteria = {};
    this.pageIndex.set(0);
    this.load();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  openDetail(employee: EmployeeResponse): void {
    this.router.navigate(['/employees', employee.id]);
  }

  private load(): void {
    this.loading.set(true);
    this.loadFailed.set(false);
    this.employeeService.search(this.appliedCriteria, this.pageIndex(), this.pageSize()).subscribe({
      next: (page) => {
        this.employees.set(page.content);
        this.totalElements.set(page.page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.employees.set([]);
        this.totalElements.set(0);
        this.loading.set(false);
        this.loadFailed.set(true);
      }
    });
  }
}
