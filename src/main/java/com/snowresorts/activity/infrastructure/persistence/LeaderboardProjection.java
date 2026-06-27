package com.snowresorts.activity.infrastructure.persistence;

import java.util.UUID;

/** Read-only projection for the leaderboard aggregate native query. */
public interface LeaderboardProjection {

    UUID getUserId();

    double getMaxSpeedKmh();

    double getTotalDistanceM();

    long getRunCount();
}
