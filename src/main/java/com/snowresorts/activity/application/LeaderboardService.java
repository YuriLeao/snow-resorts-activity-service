package com.snowresorts.activity.application;

import com.snowresorts.activity.domain.model.LeaderboardEntry;
import com.snowresorts.activity.domain.port.Runs;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Friend-leaderboard read model. Friend ids are supplied by the client (sourced from user-service)
 * for the MVP; the caller is always included. Only users with completed descents in the period are
 * ranked. Calendar windows (today / year) use the caller's device timezone. Default ordering: max
 * speed, then distance, then runs (client re-sorts by selected metric).
 *
 * <p>This is the CQRS read path; in production it is fronted by a Redis cache (TTL ~60s).
 */
@Service
@Transactional(readOnly = true)
public class LeaderboardService {

    private final Runs runs;

    public LeaderboardService(Runs runs) {
        this.runs = runs;
    }

    public List<LeaderboardEntry> friendsLeaderboard(UUID selfUserId, Collection<UUID> friendIds,
                                                     LeaderboardPeriod period, UUID resortId,
                                                     ZoneId timeZone, Instant now) {
        Set<UUID> participants = new LinkedHashSet<>();
        participants.add(selfUserId);
        if (friendIds != null) {
            participants.addAll(friendIds);
        }

        Instant since = period.since(now, timeZone);
        Instant until = period.untilExclusive(now, timeZone);
        Map<UUID, LeaderboardEntry> byUser = runs.leaderboard(participants, since, until, resortId).stream()
                .collect(Collectors.toMap(LeaderboardEntry::userId, Function.identity()));

        // Only rank users who completed at least one descent in the window — avoids
        // a zero-filled list when someone has hundreds of friends.
        return participants.stream()
                .map(byUser::get)
                .filter(entry -> entry != null)
                .sorted(Comparator
                        .comparingDouble(LeaderboardEntry::maxSpeedKmh).reversed()
                        .thenComparing(Comparator.comparingDouble(LeaderboardEntry::totalDistanceM).reversed())
                        .thenComparing(Comparator.comparingLong(LeaderboardEntry::runCount).reversed()))
                .toList();
    }
}
