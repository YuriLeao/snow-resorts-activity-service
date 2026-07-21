package com.snowresorts.activity.application;

import com.snowresorts.activity.domain.model.LeaderboardEntry;
import com.snowresorts.activity.domain.port.Runs;
import com.snowresorts.activity.domain.port.UserAccess;
import java.time.Instant;
import java.time.ZoneId;
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
 * Friend-leaderboard read model. Friend ids are resolved server-side from user-service;
 * client-supplied friend ids are ignored. The caller is always included. Only users with
 * completed descents in the period are ranked.
 */
@Service
@Transactional(readOnly = true)
public class LeaderboardService {

    private final Runs runs;
    private final UserAccess userAccess;

    public LeaderboardService(Runs runs, UserAccess userAccess) {
        this.runs = runs;
        this.userAccess = userAccess;
    }

    public List<LeaderboardEntry> friendsLeaderboard(UUID selfUserId, LeaderboardPeriod period,
                                                     UUID resortId, ZoneId timeZone, Instant now) {
        Set<UUID> participants = new LinkedHashSet<>();
        participants.add(selfUserId);
        participants.addAll(userAccess.listAcceptedFriendIds(selfUserId));

        Instant since = period.since(now, timeZone);
        Instant until = period.untilExclusive(now, timeZone);
        Map<UUID, LeaderboardEntry> byUser = runs.leaderboard(participants, since, until, resortId).stream()
                .collect(Collectors.toMap(LeaderboardEntry::userId, Function.identity()));

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
