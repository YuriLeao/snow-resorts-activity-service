package com.snowresorts.activity.domain.port;

import com.snowresorts.activity.domain.model.RunMetrics;
import java.util.Optional;
import java.util.UUID;

/** Outbound port for computed per-run metrics. */
public interface RunMetricsStore {

    void save(UUID runId, RunMetrics metrics);

    Optional<RunMetrics> findByRunId(UUID runId);
}
