package com.snowresorts.activity.application;

import com.snowresorts.activity.domain.model.Run;
import com.snowresorts.activity.domain.model.RunMetrics;
import com.snowresorts.activity.domain.model.TrackPoint;
import com.snowresorts.activity.domain.port.GpsPoints;
import com.snowresorts.activity.domain.port.RunMetricsStore;
import com.snowresorts.activity.domain.port.Runs;
import com.snowresorts.security.error.ForbiddenException;
import com.snowresorts.security.error.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read side (CQRS): run history, detail and replay, always scoped to the owner (MVP IDOR rule). */
@Service
@Transactional(readOnly = true)
public class RunQueryService {

    private final Runs runs;
    private final RunMetricsStore metricsStore;
    private final GpsPoints gpsPoints;

    public RunQueryService(Runs runs, RunMetricsStore metricsStore, GpsPoints gpsPoints) {
        this.runs = runs;
        this.metricsStore = metricsStore;
        this.gpsPoints = gpsPoints;
    }

    public RunHistoryPage history(UUID userId, LocalDate date, UUID resortId, int page, int size) {
        Runs.Page result = runs.findHistory(userId, date, resortId, page, size);
        List<RunWithMetrics> content = result.content().stream()
                .map(run -> new RunWithMetrics(run, metricsOrZero(run.id())))
                .toList();
        return new RunHistoryPage(content, result.totalElements(), result.page(), result.size());
    }

    public RunWithMetrics detail(UUID runId, UUID userId) {
        Run run = loadOwned(runId, userId);
        return new RunWithMetrics(run, metricsOrZero(run.id()));
    }

    /** @return the run's stored GPS points (ordered), for client-side polyline/GeoJSON rendering. */
    public List<TrackPoint> replay(UUID runId, UUID userId) {
        loadOwned(runId, userId);
        return gpsPoints.findByRunId(runId);
    }

    private RunMetrics metricsOrZero(UUID runId) {
        return metricsStore.findByRunId(runId).orElse(RunMetrics.zero());
    }

    private Run loadOwned(UUID runId, UUID userId) {
        Run run = runs.findById(runId)
                .orElseThrow(() -> ResourceNotFoundException.of("Run", runId));
        if (!run.isOwnedBy(userId)) {
            throw new ForbiddenException("Run %s does not belong to the current user.".formatted(runId));
        }
        return run;
    }

    public record RunWithMetrics(Run run, RunMetrics metrics) {
    }

    public record RunHistoryPage(List<RunWithMetrics> content, long totalElements, int page, int size) {
    }
}
