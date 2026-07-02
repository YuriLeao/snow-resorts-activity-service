package com.snowresorts.activity.application;

import com.snowresorts.activity.domain.metrics.MetricsCalculator;
import com.snowresorts.activity.domain.model.Run;
import com.snowresorts.activity.domain.model.RunMetrics;
import com.snowresorts.activity.domain.model.RunStatus;
import com.snowresorts.activity.domain.model.TrackPoint;
import com.snowresorts.activity.domain.port.GpsPoints;
import com.snowresorts.activity.domain.port.RunEventPublisher;
import com.snowresorts.activity.domain.port.RunMetricsStore;
import com.snowresorts.activity.domain.port.Runs;
import com.snowresorts.contracts.events.RunCompletedEvent;
import com.snowresorts.security.error.ConflictException;
import com.snowresorts.security.error.ForbiddenException;
import com.snowresorts.security.error.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Write side (CQRS): starting a run, ingesting GPS batches and finalising metrics. */
@Service
public class RunTrackingService {

    private static final Logger log = LoggerFactory.getLogger(RunTrackingService.class);

    private final Runs runs;
    private final GpsPoints gpsPoints;
    private final RunMetricsStore metricsStore;
    private final RunEventPublisher eventPublisher;
    private final MetricsCalculator metricsCalculator;

    public RunTrackingService(Runs runs, GpsPoints gpsPoints, RunMetricsStore metricsStore,
                              RunEventPublisher eventPublisher, MetricsCalculator metricsCalculator) {
        this.runs = runs;
        this.gpsPoints = gpsPoints;
        this.metricsStore = metricsStore;
        this.eventPublisher = eventPublisher;
        this.metricsCalculator = metricsCalculator;
    }

    @Transactional
    public Run start(UUID userId, UUID resortId, UUID trailId, Instant startedAt) {
        Instant start = startedAt != null ? startedAt : Instant.now();
        Run run = new Run(UUID.randomUUID(), userId, resortId, trailId, start, null, RunStatus.ACTIVE);
        Run saved = runs.save(run);
        log.debug("Started run {} for user {}", saved.id(), userId);
        return saved;
    }

    /** Appends spike-filtered GPS points to an ACTIVE run owned by the caller. */
    @Transactional
    public AppendResult appendPoints(UUID runId, UUID userId, List<TrackPoint> points) {
        Run run = loadOwned(runId, userId);
        if (run.status() != RunStatus.ACTIVE) {
            throw new ConflictException("Run %s is not active and cannot accept points.".formatted(runId));
        }
        List<TrackPoint> cleaned = metricsCalculator.clean(points);
        gpsPoints.appendAll(runId, cleaned);
        int rejected = points.size() - cleaned.size();
        log.debug("Appended {} points ({} rejected as spikes) to run {}", cleaned.size(), rejected, runId);
        return new AppendResult(cleaned.size(), rejected);
    }

    /** Finalises a run: computes metrics from stored points, persists them and publishes the event. */
    @Transactional
    public RunMetrics finish(UUID runId, UUID userId, Instant endedAt) {
        Run run = loadOwned(runId, userId);
        if (run.status() == RunStatus.COMPLETED) {
            throw new ConflictException("Run %s is already completed.".formatted(runId));
        }
        if (run.status() != RunStatus.ACTIVE) {
            throw new ConflictException("Run %s cannot be finished from status %s."
                    .formatted(runId, run.status()));
        }

        List<TrackPoint> points = gpsPoints.findByRunId(runId);
        RunMetrics metrics = metricsCalculator.calculate(points);

        Instant end = endedAt != null ? endedAt : Instant.now();
        runs.save(run.complete(end));
        metricsStore.save(runId, metrics);

        eventPublisher.publish(new RunCompletedEvent(
                UUID.randomUUID(), Instant.now(), runId, userId, run.resortId(),
                metrics.maxSpeedKmh(), metrics.distanceM(), metrics.durationSec()));

        log.debug("Finished run {}: distance={}m maxSpeed={}km/h", runId,
                metrics.distanceM(), metrics.maxSpeedKmh());
        return metrics;
    }

    /** Deletes a run owned by the caller (ACTIVE or COMPLETED). Child rows cascade in the DB. */
    @Transactional
    public void delete(UUID runId, UUID userId) {
        loadOwned(runId, userId);
        runs.deleteById(runId);
        log.debug("Deleted run {} for user {}", runId, userId);
    }

    private Run loadOwned(UUID runId, UUID userId) {
        Run run = runs.findById(runId)
                .orElseThrow(() -> ResourceNotFoundException.of("Run", runId));
        if (!run.isOwnedBy(userId)) {
            throw new ForbiddenException("Run %s does not belong to the current user.".formatted(runId));
        }
        return run;
    }

    /** Outcome of a GPS batch ingest. */
    public record AppendResult(int accepted, int rejected) {
    }
}
