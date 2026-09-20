package com.incubyte.salary.common.response;

import java.time.Instant;

/** Consistent JSON error shape returned for every handled failure. */
public record ApiError(Instant timestamp, int status, String error, String message, String path) {
}
