package com.snowresorts.activity.domain.model;

import java.time.Instant;
import java.util.UUID;

/** A descent session. Immutable; state transitions return a new instance. */
public record Run(
        UUID id,
        UUID userId,
        UUID resortId,
        UUID trailId,
        Instant startedAt,
        Instant endedAt,
        RunStatus status) {

    /** @return a COMPLETED copy with the given end timestamp. */
    public Run complete(Instant finishedAt) {
        return new Run(id, userId, resortId, trailId, startedAt, finishedAt, RunStatus.COMPLETED);
    }

    public boolean isOwnedBy(UUID candidateUserId) {
        return userId.equals(candidateUserId);
    }
}
