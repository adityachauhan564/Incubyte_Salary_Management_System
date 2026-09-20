/**
 * Mirrors backend SalaryRecordResponse. amount is a BigDecimal server-side
 * but serializes as a plain JSON number (confirmed against a live
 * response, e.g. 61204.00).
 */
export interface SalaryRecordResponse {
  id: number;
  amount: number;
  currency: string;
  /** ISO date, e.g. "2026-01-01". */
  effectiveDate: string;
  /** ISO instant, e.g. "2026-09-20T17:24:11.090Z". */
  createdAt: string;
}

/**
 * Mirrors backend SalaryRecordRequest - the body for both POST (create) and
 * PUT (correct). currency must be a 3-letter uppercase ISO 4217 code;
 * effectiveDate must be unique per employee (a duplicate is rejected 409).
 */
export interface SalaryRecordRequest {
  amount: number;
  currency: string;
  effectiveDate: string;
}
