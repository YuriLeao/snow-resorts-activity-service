package com.snowresorts.activity.domain.model;

/** Final computed metrics for a descent. */
public record RunMetrics(
        double maxSpeedKmh,
        double avgSpeedKmh,
        double distanceM,
        double maxAltitudeM,
        double verticalDropM,
        double maxInclinationDeg,
        double avgInclinationDeg,
        long durationSec) {

    public static RunMetrics zero() {
        return new RunMetrics(0, 0, 0, 0, 0, 0, 0, 0L);
    }
}
