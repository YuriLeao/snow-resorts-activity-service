package com.snowresorts.activity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(schema = "activity", name = "run_tracks_s3")
public class RunTrackEntity {

    @Id
    @Column(name = "run_id", nullable = false, updatable = false)
    private UUID runId;

    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    @Column(nullable = false, length = 20)
    private String format;

    @Column(name = "point_count", nullable = false)
    private int pointCount;

    protected RunTrackEntity() {
    }

    public RunTrackEntity(UUID runId, String s3Key, String format, int pointCount) {
        this.runId = runId;
        this.s3Key = s3Key;
        this.format = format;
        this.pointCount = pointCount;
    }

    public UUID getRunId() {
        return runId;
    }

    public String getS3Key() {
        return s3Key;
    }

    public String getFormat() {
        return format;
    }

    public int getPointCount() {
        return pointCount;
    }
}
