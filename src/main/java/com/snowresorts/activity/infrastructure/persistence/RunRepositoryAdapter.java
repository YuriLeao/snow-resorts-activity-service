package com.snowresorts.activity.infrastructure.persistence;

import com.snowresorts.activity.domain.model.LeaderboardEntry;
import com.snowresorts.activity.domain.model.Run;
import com.snowresorts.activity.domain.port.Runs;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class RunRepositoryAdapter implements Runs {

    private static final int DEFAULT_HISTORY_PAGE_SIZE = 20;
    private static final int MAX_HISTORY_PAGE_SIZE = 100;

    private final RunJpaRepository jpaRepository;

    public RunRepositoryAdapter(RunJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Run save(Run run) {
        RunEntity saved = jpaRepository.save(toEntity(run));
        return toDomain(saved);
    }

    @Override
    public Optional<Run> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Run> findHistory(UUID userId, LocalDate date, UUID resortId, int page, int size) {
        int pageSize = size <= 0 ? DEFAULT_HISTORY_PAGE_SIZE : Math.min(size, MAX_HISTORY_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize);
        org.springframework.data.domain.Slice<RunEntity> result;
        if (date != null) {
            Instant from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            result = resortId != null
                    ? jpaRepository.findByUserIdAndResortIdAndStartedAtBetweenOrderByStartedAtDesc(
                            userId, resortId, from, to, pageable)
                    : jpaRepository.findByUserIdAndStartedAtBetweenOrderByStartedAtDesc(
                            userId, from, to, pageable);
        } else if (resortId != null) {
            result = jpaRepository.findByUserIdAndResortIdOrderByStartedAtDesc(userId, resortId, pageable);
        } else {
            result = jpaRepository.findByUserIdOrderByStartedAtDesc(userId, pageable);
        }
        return result.getContent().stream().map(this::toDomain).toList();
    }

    @Override
    public List<LeaderboardEntry> leaderboard(Collection<UUID> userIds, Instant since, Instant until,
                                              UUID resortId) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<LeaderboardProjection> rows = resortId == null
                ? jpaRepository.aggregateLeaderboard(userIds, since, until)
                : jpaRepository.aggregateLeaderboardForResort(userIds, since, until, resortId);
        return rows.stream()
                .map(p -> new LeaderboardEntry(
                        p.getUserId(),
                        p.getMaxSpeedKmh(),
                        p.getTotalDistanceM(),
                        p.getRunCount(),
                        p.getTotalVerticalDropM(),
                        p.getMaxInclinationDeg(),
                        p.getTotalDurationSec()))
                .toList();
    }

    private RunEntity toEntity(Run run) {
        return new RunEntity(run.id(), run.userId(), run.resortId(), run.trailId(),
                run.startedAt(), run.endedAt(), run.status());
    }

    private Run toDomain(RunEntity entity) {
        return new Run(entity.getId(), entity.getUserId(), entity.getResortId(), entity.getTrailId(),
                entity.getStartedAt(), entity.getEndedAt(), entity.getStatus());
    }
}
