package com.snowresorts.activity.domain.port;

import com.snowresorts.activity.domain.model.LeaderboardEntry;
import com.snowresorts.activity.domain.model.Run;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound port for descent-session persistence and read queries. */
public interface Runs {

    Run save(Run run);

    Optional<Run> findById(UUID id);

    void deleteById(UUID id);

    /** Paginated history for a single owner, optionally filtered by UTC date and resort. */
    List<Run> findHistory(UUID userId, LocalDate date, UUID resortId, int page, int size);

    /**
     * Aggregates COMPLETED runs with {@code started_at} in {@code [since, until)} for the given
     * users, joined with their metrics. When {@code resortId} is non-null, only descents at that
     * resort are included.
     *
     * @return one entry per user that has at least one qualifying run.
     */
    List<LeaderboardEntry> leaderboard(Collection<UUID> userIds, Instant since, Instant until,
                                       UUID resortId);
}
