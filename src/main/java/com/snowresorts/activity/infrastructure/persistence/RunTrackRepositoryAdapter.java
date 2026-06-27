package com.snowresorts.activity.infrastructure.persistence;

import com.snowresorts.activity.domain.port.RunTracks;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class RunTrackRepositoryAdapter implements RunTracks {

    private final RunTrackJpaRepository jpaRepository;

    public RunTrackRepositoryAdapter(RunTrackJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(UUID runId, String s3Key, String format, int pointCount) {
        jpaRepository.save(new RunTrackEntity(runId, s3Key, format, pointCount));
    }

    @Override
    public Optional<TrackRef> findByRunId(UUID runId) {
        return jpaRepository.findById(runId)
                .map(e -> new TrackRef(e.getRunId(), e.getS3Key(), e.getFormat(), e.getPointCount()));
    }
}
