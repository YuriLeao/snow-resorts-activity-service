package com.snowresorts.activity.infrastructure.persistence;

import com.snowresorts.activity.domain.model.RunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "activity", name = "runs")
public class RunEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "resort_id")
    private UUID resortId;

    @Column(name = "trail_id")
    private UUID trailId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RunStatus status;

    protected RunEntity() {
    }

    public RunEntity(UUID id, UUID userId, UUID resortId, UUID trailId,
                     Instant startedAt, Instant endedAt, RunStatus status) {
        this.id = id;
        this.userId = userId;
        this.resortId = resortId;
        this.trailId = trailId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getResortId() {
        return resortId;
    }

    public UUID getTrailId() {
        return trailId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public RunStatus getStatus() {
        return status;
    }
}
