package com.snowresorts.activity.domain.model;

import java.util.UUID;

/** One ranked competitor in a friend leaderboard for a given period. */
public record LeaderboardEntry(
        UUID userId,
        double maxSpeedKmh,
        double totalDistanceM,
        long runCount,
        double totalVerticalDropM,
        double maxInclinationDeg,
        long totalDurationSec) {}

