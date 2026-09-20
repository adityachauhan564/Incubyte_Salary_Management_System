import { HttpContext, HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { ApiError } from './models/api-error.model';

/**
 * Set on a request's HttpContext to opt out of the generic error snackbar
 * for an expected error a component wants to render its own UI state for
 * instead (e.g. current-salary 404 -> "No current salary record", not a
 * generic error toast). Usage: http.get(url, { context: withoutErrorSnackbar() }).
 */
export const SUPPRESS_ERROR_SNACKBAR = new HttpContextToken<boolean>(() => false);

export function withoutErrorSnackbar(): HttpContext {
  return new HttpContext().set(SUPPRESS_ERROR_SNACKBAR, true);
}

/**
 * Single place every non-2xx response is turned into user feedback,
 * mirroring the backend's own single GlobalExceptionHandler - components
 * don't each implement their own generic error handling.
 */
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!req.context.get(SUPPRESS_ERROR_SNACKBAR)) {
        const apiError = error.error as ApiError | undefined;
        const message = apiError?.message ?? 'Something went wrong. Please try again.';
        snackBar.open(message, 'Dismiss', { duration: 5000 });
      }
      return throwError(() => error);
    })
  );
};
