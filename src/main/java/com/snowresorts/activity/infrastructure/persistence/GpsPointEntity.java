package com.snowresorts.activity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "activity", name = "gps_points")
public class GpsPointEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column
    private Double altitude;

    @Column(name = "speed_kmh")
    private Double speedKmh;

    protected GpsPointEntity() {
    }

    public GpsPointEntity(UUID runId, Instant recordedAt, double lat, double lng,
                          Double altitude, Double speedKmh) {
        this.id = UUID.randomUUID();
        this.runId = runId;
        this.recordedAt = recordedAt;
        this.lat = lat;
        this.lng = lng;
        this.altitude = altitude;
        this.speedKmh = speedKmh;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRunId() {
        return runId;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public Double getAltitude() {
        return altitude;
    }

    public Double getSpeedKmh() {
        return speedKmh;
    }
}
