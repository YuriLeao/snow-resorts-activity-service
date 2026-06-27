package com.snowresorts.activity.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Rolling time windows for friend leaderboards. */
public enum LeaderboardPeriod {

    /** From the start of the current UTC day. */
    TODAY,
    /** The trailing 7 days. */
    WEEK,
    /** The trailing season (~180 days) — a pragmatic MVP approximation. */
    SEASON;

    public static LeaderboardPeriod fromParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return TODAY;
        }
        return LeaderboardPeriod.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    /** @return the inclusive lower bound for {@code started_at} given the current instant. */
    public Instant since(Instant now) {
        return switch (this) {
            case TODAY -> now.truncatedTo(ChronoUnit.DAYS);
            case WEEK -> now.minus(7, ChronoUnit.DAYS);
            case SEASON -> now.minus(180, ChronoUnit.DAYS);
        };
    }
}
