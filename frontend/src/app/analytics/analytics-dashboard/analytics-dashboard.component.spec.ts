import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Subject, of } from 'rxjs';

import { AnalyticsDashboardComponent } from './analytics-dashboard.component';
import { AnalyticsService } from '../analytics.service';
import { GroupedSalaryStats, SalaryRangeBucket, SalarySummary } from '../../core/models/analytics.model';

describe('AnalyticsDashboardComponent', () => {
  let fixture: ComponentFixture<AnalyticsDashboardComponent>;
  let component: AnalyticsDashboardComponent;
  let analyticsService: jasmine.SpyObj<AnalyticsService>;

  const summary: SalarySummary = {
    totalEmployees: 10000,
    totalSalaryCost: 950000000,
    averageSalary: 95000,
    medianSalary: 92000,
    minSalary: 42000,
    maxSalary: 210000,
    currency: 'USD'
  };

  const departments: GroupedSalaryStats[] = [
    { name: 'Engineering', headcount: 1200, totalCost: 114000000, averageSalary: 95000, minSalary: 60000, maxSalary: 210000, currency: 'USD' }
  ];

  const countries: GroupedSalaryStats[] = [
    { name: 'India', headcount: 3000, totalCost: 200000000, averageSalary: 66666, minSalary: 40000, maxSalary: 180000, currency: 'USD' }
  ];

  const buckets: SalaryRangeBucket[] = [
    { label: '50000.00 - 75000.00', rangeStart: 50000, rangeEnd: 75000, count: 1450, currency: 'USD' }
  ];

  function setup(): void {
    fixture = TestBed.createComponent(AnalyticsDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    analyticsService = jasmine.createSpyObj('AnalyticsService', [
      'getSummary',
      'getByDepartment',
      'getByCountry',
      'getDistribution'
    ]);

    TestBed.configureTestingModule({
      imports: [AnalyticsDashboardComponent],
      providers: [{ provide: AnalyticsService, useValue: analyticsService }, provideNoopAnimations()]
    });
  });

  it('renders summary stat cards on success', () => {
    analyticsService.getSummary.and.returnValue(of(summary));
    analyticsService.getByDepartment.and.returnValue(of(departments));
    analyticsService.getByCountry.and.returnValue(of(countries));
    analyticsService.getDistribution.and.returnValue(of(buckets));

    setup();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('10,000');
    expect(text).toContain('950,000,000.00');
    expect(text).toContain('95,000.00');
  });

  it('shows a loading spinner while the summary request is in flight', () => {
    const subject = new Subject<SalarySummary>();
    analyticsService.getSummary.and.returnValue(subject.asObservable());
    analyticsService.getByDepartment.and.returnValue(of([]));
    analyticsService.getByCountry.and.returnValue(of([]));
    analyticsService.getDistribution.and.returnValue(of([]));

    setup();

    expect(fixture.nativeElement.querySelector('mat-spinner')).toBeTruthy();
    subject.next(summary);
    subject.complete();
  });

  it('builds chart data from department and country stats', () => {
    analyticsService.getSummary.and.returnValue(of(summary));
    analyticsService.getByDepartment.and.returnValue(of(departments));
    analyticsService.getByCountry.and.returnValue(of(countries));
    analyticsService.getDistribution.and.returnValue(of(buckets));

    setup();

    expect(component.departmentAverageChart()).toEqual([{ name: 'Engineering', value: 95000 }]);
    expect(component.departmentCostChart()).toEqual([{ name: 'Engineering', value: 114000000 }]);
    expect(component.countryAverageChart()).toEqual([{ name: 'India', value: 66666 }]);
    expect(component.distributionChart()).toEqual([{ name: '50000.00 - 75000.00', value: 1450 }]);
  });

  it('reloads the distribution with the new bucket size when Apply is clicked', () => {
    analyticsService.getSummary.and.returnValue(of(summary));
    analyticsService.getByDepartment.and.returnValue(of(departments));
    analyticsService.getByCountry.and.returnValue(of(countries));
    analyticsService.getDistribution.and.returnValue(of(buckets));

    setup();
    analyticsService.getDistribution.calls.reset();

    component.bucketSizeInput = 50000;
    component.applyBucketSize();

    expect(analyticsService.getDistribution).toHaveBeenCalledWith(50000);
  });

  it('shows "No department data yet." when the department list is empty', () => {
    analyticsService.getSummary.and.returnValue(of(summary));
    analyticsService.getByDepartment.and.returnValue(of([]));
    analyticsService.getByCountry.and.returnValue(of(countries));
    analyticsService.getDistribution.and.returnValue(of(buckets));

    setup();

    expect(fixture.nativeElement.textContent).toContain('No department data yet.');
  });
});
