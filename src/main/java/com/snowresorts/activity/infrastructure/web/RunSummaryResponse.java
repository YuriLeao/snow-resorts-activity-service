package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.application.RunQueryService.RunWithMetrics;
import com.snowresorts.activity.domain.model.Run;
import com.snowresorts.activity.domain.model.RunMetrics;
import java.time.Instant;
import java.util.UUID;

public record RunSummaryResponse(
        UUID runId,
        UUID resortId,
        UUID trailId,
        Instant startedAt,
        Instant endedAt,
        String status,
        RunMetricsResponse metrics) {

    public static RunSummaryResponse of(Run run, RunMetrics metrics) {
        return new RunSummaryResponse(run.id(), run.resortId(), run.trailId(), run.startedAt(),
                run.endedAt(), run.status().name(), RunMetricsResponse.from(metrics));
    }

    public static RunSummaryResponse from(RunWithMetrics rwm) {
        return of(rwm.run(), rwm.metrics());
    }
}
