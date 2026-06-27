package com.snowresorts.activity.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

/**
 * Starts a descent. {@code resortId}/{@code trailId} are optional (may be resolved later via spatial
 * match); {@code startedAt} defaults to server time when omitted.
 */
public record StartRunRequest(
        UUID resortId,
        UUID trailId,
        Instant startedAt) {
}
