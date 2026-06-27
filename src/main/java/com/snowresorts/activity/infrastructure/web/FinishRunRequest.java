package com.snowresorts.activity.infrastructure.web;

import java.time.Instant;

/** Optional finish payload; {@code endedAt} defaults to server time when omitted. */
public record FinishRunRequest(Instant endedAt) {
}
