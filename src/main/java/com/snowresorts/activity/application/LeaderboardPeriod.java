package com.snowresorts.activity.application;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Rolling / calendar time windows for friend leaderboards. */
public enum LeaderboardPeriod {

    /**
     * From local midnight in the caller's timezone so "today" matches the calendar
     * day on their phone.
     */
    TODAY,
    /** The trailing 7 days. */
    WEEK,
    /** The trailing season (~180 days) — a pragmatic MVP approximation. */
    SEASON,
    /**
     * Calendar year in the caller's timezone: from 1 Jan 00:00 inclusive through
     * 31 Dec 23:59:59.999… (implemented as {@code started_at <} 1 Jan next year).
     */
    YEAR,
    /** No lower bound — all completed descents. */
    ALL;

    private static final Logger log = LoggerFactory.getLogger(LeaderboardPeriod.class);

    /** Fallback when the client omits or sends an unknown IANA zone id. */
    static final ZoneId FALLBACK_ZONE = ZoneOffset.UTC;

    /** Open-ended upper bound for periods that are not a closed calendar year. */
    private static final Instant OPEN_ENDED_UNTIL = Instant.parse("9999-01-01T00:00:00Z");

    public static LeaderboardPeriod fromParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }
        return LeaderboardPeriod.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * Resolves an IANA timezone id from the client (e.g. {@code America/Santiago}).
     * Unknown or blank values fall back to UTC so ranking still works.
     */
    public static ZoneId resolveZone(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            return FALLBACK_ZONE;
        }
        try {
            return ZoneId.of(timeZone.trim());
        } catch (DateTimeException ex) {
            log.warn("Unknown leaderboard timeZone '{}', falling back to UTC", timeZone);
            return FALLBACK_ZONE;
        }
    }

    /** Inclusive lower bound for {@code started_at}, in the caller's local calendar. */
    public Instant since(Instant now, ZoneId zone) {
        return switch (this) {
            case TODAY -> now.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant();
            case WEEK -> now.minus(7, ChronoUnit.DAYS);
            case SEASON -> now.minus(180, ChronoUnit.DAYS);
            case YEAR -> yearStart(now.atZone(zone).getYear(), zone);
            case ALL -> Instant.EPOCH;
        };
    }

    /**
     * Exclusive upper bound for {@code started_at} ({@code started_at < until}).
     * For {@link #YEAR} this is 1 Jan 00:00 of the following year in {@code zone}, so the
     * window covers through 31 Dec 23:59:59.999… of the current local year.
     */
    public Instant untilExclusive(Instant now, ZoneId zone) {
        return switch (this) {
            case YEAR -> yearStart(now.atZone(zone).getYear() + 1, zone);
            case TODAY, WEEK, SEASON, ALL -> OPEN_ENDED_UNTIL;
        };
    }

    private static Instant yearStart(int year, ZoneId zone) {
        return LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant();
    }
}
