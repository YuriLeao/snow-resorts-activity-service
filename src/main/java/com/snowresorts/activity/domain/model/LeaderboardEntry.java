package com.snowresorts.activity.domain.model;

import java.util.UUID;

/** One ranked competitor in a friend leaderboard for a given period. */
public record LeaderboardEntry(
        UUID userId,
        double maxSpeedKmh,
        double totalDistanceM,
        long runCount) {

    public static LeaderboardEntry empty(UUID userId) {
        return new LeaderboardEntry(userId, 0, 0, 0L);
    }
}
