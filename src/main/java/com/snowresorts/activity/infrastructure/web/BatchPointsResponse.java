package com.snowresorts.activity.infrastructure.web;

/**
 * Result of a GPS batch ingest. {@code duplicate} is {@code true} when an {@code Idempotency-Key}
 * replay was detected and the batch was skipped.
 */
public record BatchPointsResponse(int accepted, int rejected, boolean duplicate) {
}
