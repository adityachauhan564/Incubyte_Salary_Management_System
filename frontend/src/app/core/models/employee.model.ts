/** Mirrors backend EmployeeResponse (employee/dto/EmployeeResponse.java). */
export interface EmployeeResponse {
  id: number;
  firstName: string;
  lastName: string;
  country: string;
  department: string;
  jobTitle: string;
}

/**
 * Mirrors backend EmployeeSearchCriteria. All fields optional - omitted/
 * undefined fields are not sent as query params and are not applied
 * server-side (EmployeeSpecifications.matching()).
 */
export interface EmployeeSearchCriteria {
  search?: string;
  country?: string;
  department?: string;
  jobTitle?: string;
}
