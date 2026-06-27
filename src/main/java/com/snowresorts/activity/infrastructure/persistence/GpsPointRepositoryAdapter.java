package com.snowresorts.activity.infrastructure.persistence;

import com.snowresorts.activity.domain.model.TrackPoint;
import com.snowresorts.activity.domain.port.GpsPoints;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class GpsPointRepositoryAdapter implements GpsPoints {

    private final GpsPointJpaRepository jpaRepository;

    public GpsPointRepositoryAdapter(GpsPointJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void appendAll(UUID runId, List<TrackPoint> points) {
        // Domain TrackPoint carries no inclination; it is derived in metrics, so stored as null here.
        List<GpsPointEntity> entities = points.stream()
                .map(p -> new GpsPointEntity(
                        runId, p.recordedAt(), p.lat(), p.lng(), p.altitude(), p.speedKmh(), null))
                .toList();
        jpaRepository.saveAll(entities);
    }

    @Override
    public List<TrackPoint> findByRunId(UUID runId) {
        return jpaRepository.findByRunIdOrderByRecordedAtAsc(runId).stream()
                .map(e -> new TrackPoint(
                        e.getRecordedAt(), e.getLat(), e.getLng(), e.getAltitude(), e.getSpeedKmh()))
                .toList();
    }
}
