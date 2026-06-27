package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.domain.model.LeaderboardEntry;
import java.util.UUID;

public record LeaderboardEntryResponse(
        int rank,
        UUID userId,
        double maxSpeedKmh,
        double totalDistanceM,
        long runCount) {

    public static LeaderboardEntryResponse of(int rank, LeaderboardEntry entry) {
        return new LeaderboardEntryResponse(rank, entry.userId(), entry.maxSpeedKmh(),
                entry.totalDistanceM(), entry.runCount());
    }
}
