package com.snowresorts.activity.domain.model;

import java.time.Instant;

/**
 * A single GPS sample fed to the {@code MetricsCalculator}. {@code altitude} and
 * {@code speedKmh} are nullable because mobile devices do not always report them.
 */
public record TrackPoint(
        Instant recordedAt,
        double lat,
        double lng,
        Double altitude,
        Double speedKmh) {
}
