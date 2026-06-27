package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.domain.model.RunMetrics;

public record RunMetricsResponse(
        double maxSpeedKmh,
        double avgSpeedKmh,
        double distanceM,
        double verticalDropM,
        double maxInclinationDeg,
        double avgInclinationDeg,
        long durationSec) {

    public static RunMetricsResponse from(RunMetrics m) {
        return new RunMetricsResponse(m.maxSpeedKmh(), m.avgSpeedKmh(), m.distanceM(),
                m.verticalDropM(), m.maxInclinationDeg(), m.avgInclinationDeg(), m.durationSec());
    }
}
