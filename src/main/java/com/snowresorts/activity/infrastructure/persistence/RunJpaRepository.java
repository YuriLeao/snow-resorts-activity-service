package com.snowresorts.activity.infrastructure.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RunJpaRepository extends JpaRepository<RunEntity, UUID> {

    Slice<RunEntity> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    Slice<RunEntity> findByUserIdAndResortIdOrderByStartedAtDesc(UUID userId, UUID resortId, Pageable pageable);

    Slice<RunEntity> findByUserIdAndStartedAtBetweenOrderByStartedAtDesc(
            UUID userId, Instant from, Instant to, Pageable pageable);

    Slice<RunEntity> findByUserIdAndResortIdAndStartedAtBetweenOrderByStartedAtDesc(
            UUID userId, UUID resortId, Instant from, Instant to, Pageable pageable);

    @Query(value = """
            SELECT r.user_id AS userId,
                   COALESCE(MAX(m.max_speed_kmh), 0) AS maxSpeedKmh,
                   COALESCE(SUM(m.distance_m), 0) AS totalDistanceM,
                   COUNT(*) AS runCount
            FROM activity.runs r
            LEFT JOIN activity.run_metrics m ON m.run_id = r.id
            WHERE r.user_id IN (:userIds)
              AND r.status = 'COMPLETED'
              AND r.started_at >= :since
            GROUP BY r.user_id
            """, nativeQuery = true)
    List<LeaderboardProjection> aggregateLeaderboard(@Param("userIds") Collection<UUID> userIds,
                                                     @Param("since") Instant since);
}
