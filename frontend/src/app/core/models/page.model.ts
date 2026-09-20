/**
 * Mirrors Spring Data's PagedModel<T>, as returned by GET /api/employees.
 * Confirmed against the live response shape:
 * { "content": [...], "page": { "size", "number", "totalElements", "totalPages" } }
 */
export interface PageMetadata {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface PagedModel<T> {
  content: T[];
  page: PageMetadata;
}
