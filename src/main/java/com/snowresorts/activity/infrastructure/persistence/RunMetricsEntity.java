package com.snowresorts.activity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(schema = "activity", name = "run_metrics")
public class RunMetricsEntity {

    @Id
    @Column(name = "run_id", nullable = false, updatable = false)
    private UUID runId;

    @Column(name = "max_speed_kmh", nullable = false)
    private double maxSpeedKmh;

    @Column(name = "avg_speed_kmh", nullable = false)
    private double avgSpeedKmh;

    @Column(name = "distance_m", nullable = false)
    private double distanceM;

    @Column(name = "vertical_drop_m", nullable = false)
    private double verticalDropM;

    @Column(name = "max_inclination_deg", nullable = false)
    private double maxInclinationDeg;

    @Column(name = "avg_inclination_deg", nullable = false)
    private double avgInclinationDeg;

    @Column(name = "duration_sec", nullable = false)
    private long durationSec;

    protected RunMetricsEntity() {
    }

    public RunMetricsEntity(UUID runId, double maxSpeedKmh, double avgSpeedKmh, double distanceM,
                            double verticalDropM, double maxInclinationDeg, double avgInclinationDeg,
                            long durationSec) {
        this.runId = runId;
        this.maxSpeedKmh = maxSpeedKmh;
        this.avgSpeedKmh = avgSpeedKmh;
        this.distanceM = distanceM;
        this.verticalDropM = verticalDropM;
        this.maxInclinationDeg = maxInclinationDeg;
        this.avgInclinationDeg = avgInclinationDeg;
        this.durationSec = durationSec;
    }

    public UUID getRunId() {
        return runId;
    }

    public double getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    public double getAvgSpeedKmh() {
        return avgSpeedKmh;
    }

    public double getDistanceM() {
        return distanceM;
    }

    public double getVerticalDropM() {
        return verticalDropM;
    }

    public double getMaxInclinationDeg() {
        return maxInclinationDeg;
    }

    public double getAvgInclinationDeg() {
        return avgInclinationDeg;
    }

    public long getDurationSec() {
        return durationSec;
    }
}
