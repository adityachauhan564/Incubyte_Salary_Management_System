import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AnalyticsService } from '../analytics.service';
import { GroupedSalaryStats, SalaryRangeBucket, SalarySummary } from '../../core/models/analytics.model';

interface ChartDatum {
  name: string;
  value: number;
}

const DEFAULT_BUCKET_SIZE = 25000;

@Component({
  selector: 'app-analytics-dashboard',
  imports: [
    FormsModule,
    NgxChartsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './analytics-dashboard.component.html',
  styleUrl: './analytics-dashboard.component.scss'
})
export class AnalyticsDashboardComponent {
  private readonly analyticsService = inject(AnalyticsService);

  readonly summary = signal<SalarySummary | null>(null);
  readonly loadingSummary = signal(true);

  readonly departments = signal<GroupedSalaryStats[]>([]);
  readonly loadingDepartments = signal(true);

  readonly countries = signal<GroupedSalaryStats[]>([]);
  readonly loadingCountries = signal(true);

  readonly distribution = signal<SalaryRangeBucket[]>([]);
  readonly loadingDistribution = signal(true);

  // Bound to the bucket-size input; applied on "Apply".
  bucketSizeInput = DEFAULT_BUCKET_SIZE;
  private readonly appliedBucketSize = signal(DEFAULT_BUCKET_SIZE);

  readonly departmentAverageChart = computed<ChartDatum[]>(() =>
    this.departments().map((d) => ({ name: d.name, value: d.averageSalary }))
  );
  readonly departmentCostChart = computed<ChartDatum[]>(() =>
    this.departments().map((d) => ({ name: d.name, value: d.totalCost }))
  );
  readonly countryAverageChart = computed<ChartDatum[]>(() =>
    this.countries().map((c) => ({ name: c.name, value: c.averageSalary }))
  );
  readonly countryCostChart = computed<ChartDatum[]>(() =>
    this.countries().map((c) => ({ name: c.name, value: c.totalCost }))
  );
  readonly distributionChart = computed<ChartDatum[]>(() =>
    this.distribution().map((b) => ({ name: b.label, value: b.count }))
  );

  constructor() {
    this.loadSummary();
    this.loadDepartments();
    this.loadCountries();
    this.loadDistribution();
  }

  applyBucketSize(): void {
    if (this.bucketSizeInput > 0) {
      this.appliedBucketSize.set(this.bucketSizeInput);
      this.loadDistribution();
    }
  }

  private loadSummary(): void {
    this.loadingSummary.set(true);
    this.analyticsService.getSummary().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loadingSummary.set(false);
      },
      error: () => this.loadingSummary.set(false)
    });
  }

  private loadDepartments(): void {
    this.loadingDepartments.set(true);
    this.analyticsService.getByDepartment().subscribe({
      next: (stats) => {
        this.departments.set(stats);
        this.loadingDepartments.set(false);
      },
      error: () => this.loadingDepartments.set(false)
    });
  }

  private loadCountries(): void {
    this.loadingCountries.set(true);
    this.analyticsService.getByCountry().subscribe({
      next: (stats) => {
        this.countries.set(stats);
        this.loadingCountries.set(false);
      },
      error: () => this.loadingCountries.set(false)
    });
  }

  private loadDistribution(): void {
    this.loadingDistribution.set(true);
    this.analyticsService.getDistribution(this.appliedBucketSize()).subscribe({
      next: (buckets) => {
        this.distribution.set(buckets);
        this.loadingDistribution.set(false);
      },
      error: () => this.loadingDistribution.set(false)
    });
  }
}
