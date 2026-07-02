package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.application.RunQueryService.RunWithMetrics;
import java.time.Instant;
import java.util.UUID;

public record RunDetailResponse(
        UUID runId,
        UUID userId,
        UUID resortId,
        UUID trailId,
        Instant startedAt,
        Instant endedAt,
        String status,
        RunMetricsResponse metrics) {

    public static RunDetailResponse from(RunWithMetrics rwm) {
        var run = rwm.run();
        return new RunDetailResponse(run.id(), run.userId(), run.resortId(), run.trailId(),
                run.startedAt(), run.endedAt(), run.status().name(),
                RunMetricsResponse.from(rwm.metrics()));
    }
}
