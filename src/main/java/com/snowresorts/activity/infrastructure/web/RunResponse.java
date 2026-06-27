package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.domain.model.Run;
import java.time.Instant;
import java.util.UUID;

public record RunResponse(
        UUID runId,
        UUID userId,
        UUID resortId,
        UUID trailId,
        Instant startedAt,
        Instant endedAt,
        String status) {

    public static RunResponse from(Run run) {
        return new RunResponse(run.id(), run.userId(), run.resortId(), run.trailId(),
                run.startedAt(), run.endedAt(), run.status().name());
    }
}
