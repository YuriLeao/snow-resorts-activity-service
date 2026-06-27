package com.snowresorts.activity.domain.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for the S3 reference of a run's full compressed track. Unused in dev (no S3),
 * but kept so the write path can persist a reference once an object-storage adapter exists.
 */
public interface RunTracks {

    void save(UUID runId, String s3Key, String format, int pointCount);

    Optional<TrackRef> findByRunId(UUID runId);

    record TrackRef(UUID runId, String s3Key, String format, int pointCount) {
    }
}
