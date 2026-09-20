/** Mirrors backend ApiError - the shape of every non-2xx response body. */
export interface ApiError {
  timestamp: string;
  status: number;
  /** e.g. EMPLOYEE_NOT_FOUND, VALIDATION_ERROR, DUPLICATE_SALARY_RECORD, UNSUPPORTED_CURRENCY, INTERNAL_ERROR. */
  error: string;
  message: string;
  path: string;
}
