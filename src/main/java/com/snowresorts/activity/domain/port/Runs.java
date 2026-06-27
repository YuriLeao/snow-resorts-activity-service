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

    /** Paginated history for a single owner, optionally filtered by UTC date and resort. */
    Page findHistory(UUID userId, LocalDate date, UUID resortId, int page, int size);

    /**
     * Aggregates COMPLETED runs since {@code since} for the given users, joined with their metrics.
     *
     * @return one entry per user that has at least one qualifying run.
     */
    List<LeaderboardEntry> leaderboard(Collection<UUID> userIds, Instant since);

    /** A page of runs plus the total count, keeping Spring Data types out of the domain. */
    record Page(List<Run> content, long totalElements, int page, int size) {
    }
}
