package com.snowresorts.activity.application;

import com.snowresorts.activity.domain.model.Run;
import com.snowresorts.activity.domain.model.RunMetrics;
import com.snowresorts.activity.domain.model.RunStatus;
import com.snowresorts.activity.domain.model.TrackPoint;
import com.snowresorts.activity.domain.port.GpsPoints;
import com.snowresorts.activity.domain.port.RunMetricsStore;
import com.snowresorts.activity.domain.port.Runs;
import com.snowresorts.activity.domain.port.UserAccess;
import com.snowresorts.security.error.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read side (CQRS): run history, detail and replay. Non-owners may only access COMPLETED runs
 * when user-service reports friendship + {@code shareStats} permission.
 */
@Service
@Transactional(readOnly = true)
public class RunQueryService {

    private final Runs runs;
    private final RunMetricsStore metricsStore;
    private final GpsPoints gpsPoints;
    private final UserAccess userAccess;

    public RunQueryService(Runs runs, RunMetricsStore metricsStore, GpsPoints gpsPoints,
                           UserAccess userAccess) {
        this.runs = runs;
        this.metricsStore = metricsStore;
        this.gpsPoints = gpsPoints;
        this.userAccess = userAccess;
    }

    public RunHistoryPage history(UUID subjectUserId, UUID viewerId, LocalDate date, UUID resortId,
                                  int page, int size) {
        requireStatsAccess(viewerId, subjectUserId);
        List<RunWithMetrics> content = runs.findHistory(subjectUserId, date, resortId, page, size).stream()
                .map(run -> new RunWithMetrics(run, metricsOrZero(run.id())))
                .toList();
        return new RunHistoryPage(content);
    }

    public RunWithMetrics detail(UUID runId, UUID viewerId) {
        Run run = loadAccessible(runId, viewerId);
        return new RunWithMetrics(run, metricsOrZero(run.id()));
    }

    /** @return the run's stored GPS points (ordered), for client-side polyline/GeoJSON rendering. */
    public List<TrackPoint> trackPoints(UUID runId, UUID viewerId) {
        loadAccessible(runId, viewerId);
        return gpsPoints.findByRunId(runId);
    }

    private RunMetrics metricsOrZero(UUID runId) {
        return metricsStore.findByRunId(runId).orElse(RunMetrics.zero());
    }

    private Run loadAccessible(UUID runId, UUID viewerId) {
        Run run = runs.findById(runId)
                .orElseThrow(() -> ResourceNotFoundException.of("Run", runId));
        if (run.isOwnedBy(viewerId)) {
            return run;
        }
        if (run.status() == RunStatus.COMPLETED && userAccess.canViewStats(viewerId, run.userId())) {
            return run;
        }
        // 404 (not 403) to avoid confirming existence to unauthorized callers.
        throw ResourceNotFoundException.of("Run", runId);
    }

    private void requireStatsAccess(UUID viewerId, UUID ownerId) {
        if (!userAccess.canViewStats(viewerId, ownerId)) {
            throw ResourceNotFoundException.of("Profile", ownerId);
        }
    }

    public record RunWithMetrics(Run run, RunMetrics metrics) {
    }

    public record RunHistoryPage(List<RunWithMetrics> content) {
    }
}
