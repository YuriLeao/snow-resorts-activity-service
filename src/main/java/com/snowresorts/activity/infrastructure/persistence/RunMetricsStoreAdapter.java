package com.snowresorts.activity.infrastructure.persistence;

import com.snowresorts.activity.domain.model.RunMetrics;
import com.snowresorts.activity.domain.port.RunMetricsStore;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class RunMetricsStoreAdapter implements RunMetricsStore {

    private final RunMetricsJpaRepository jpaRepository;

    public RunMetricsStoreAdapter(RunMetricsJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(UUID runId, RunMetrics metrics) {
        jpaRepository.save(new RunMetricsEntity(
                runId, metrics.maxSpeedKmh(), metrics.avgSpeedKmh(), metrics.distanceM(),
                metrics.maxAltitudeM(), metrics.verticalDropM(), metrics.maxInclinationDeg(),
                metrics.avgInclinationDeg(), metrics.durationSec()));
    }

    @Override
    public Optional<RunMetrics> findByRunId(UUID runId) {
        return jpaRepository.findById(runId).map(this::toDomain);
    }

    private RunMetrics toDomain(RunMetricsEntity e) {
        return new RunMetrics(e.getMaxSpeedKmh(), e.getAvgSpeedKmh(), e.getDistanceM(),
                e.getMaxAltitudeM(), e.getVerticalDropM(), e.getMaxInclinationDeg(),
                e.getAvgInclinationDeg(), e.getDurationSec());
    }
}
