/**
 * Mirrors backend SalarySummary. totalEmployees counts every employee;
 * average/median/min/max are null if no employee currently has a salary.
 */
export interface SalarySummary {
  totalEmployees: number;
  totalSalaryCost: number;
  averageSalary: number | null;
  medianSalary: number | null;
  minSalary: number | null;
  maxSalary: number | null;
  currency: string;
}

/** Mirrors backend GroupedSalaryStats - one entry per department or country. */
export interface GroupedSalaryStats {
  name: string;
  headcount: number;
  totalCost: number;
  averageSalary: number;
  minSalary: number;
  maxSalary: number;
  currency: string;
}

/** Mirrors backend SalaryRangeBucket. rangeEnd is exclusive. */
export interface SalaryRangeBucket {
  label: string;
  rangeStart: number;
  rangeEnd: number;
  count: number;
  currency: string;
}
