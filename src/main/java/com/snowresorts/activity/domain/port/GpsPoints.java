package com.snowresorts.activity.domain.port;

import com.snowresorts.activity.domain.model.TrackPoint;
import java.util.List;
import java.util.UUID;

/** Outbound port for raw GPS sample storage. */
public interface GpsPoints {

    void appendAll(UUID runId, List<TrackPoint> points);

    /** @return all points for a run ordered by {@code recordedAt} ascending. */
    List<TrackPoint> findByRunId(UUID runId);
}
