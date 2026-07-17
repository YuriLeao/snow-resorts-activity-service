package com.snowresorts.activity.domain.metrics;

import com.snowresorts.activity.domain.model.RunMetrics;
import com.snowresorts.activity.domain.model.TrackPoint;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure descent-metrics engine (no Spring). Operates on an ordered list of {@link TrackPoint}s.
 *
 * <p><b>GPS spike rejection (recommendation #12).</b> Before any metric is computed the track is
 * cleaned: a point is discarded when, relative to the last accepted point, the implied speed
 * exceeds {@value #MAX_SPEED_KMH} km/h OR the jump distance exceeds {@value #MAX_JUMP_M} m. The
 * previously accepted point stays as the anchor, so a single outlier cannot drag the whole track.
 *
 * <p><b>Altitude.</b> {@code maxAltitudeM} is the highest sample with altitude data.
 * {@code verticalDropM} is {@code max(altitude) - min(altitude)} over the same samples — the
 * vertical range experienced during the run, regardless of net uphill/downhill order.
 *
 * <p><b>Inclination.</b> Per segment {@code atan2(deltaAltitude, horizontalDistance)} in degrees;
 * only segments with a positive horizontal distance and altitudes on both ends are considered.
 * {@code maxInclinationDeg}/{@code avgInclinationDeg} use the absolute value of each segment angle.
 */
public class MetricsCalculator {

    /** Samples implying a speed above this (km/h) are treated as GPS spikes. */
    public static final double MAX_SPEED_KMH = 150.0;
    /** Consecutive samples further apart than this (metres) are treated as GPS spikes. */
    public static final double MAX_JUMP_M = 500.0;

    /**
     * Removes GPS spikes, keeping the first point and every subsequent point that is plausibly
     * reachable from the last accepted one.
     *
     * @return a new cleaned list (never {@code null}); inputs of size &le; 1 are returned as-is.
     */
    public List<TrackPoint> clean(List<TrackPoint> points) {
        List<TrackPoint> cleaned = new ArrayList<>();
        if (points == null || points.isEmpty()) {
            return cleaned;
        }
        TrackPoint anchor = points.get(0);
        cleaned.add(anchor);
        for (int i = 1; i < points.size(); i++) {
            TrackPoint candidate = points.get(i);
            double jumpM = Haversine.distanceMeters(
                    anchor.lat(), anchor.lng(), candidate.lat(), candidate.lng());
            double seconds = secondsBetween(anchor, candidate);
            double impliedSpeedKmh = seconds > 0 ? (jumpM / seconds) * 3.6 : 0.0;
            if (jumpM > MAX_JUMP_M || impliedSpeedKmh > MAX_SPEED_KMH) {
                continue; // spike: drop candidate, keep current anchor
            }
            cleaned.add(candidate);
            anchor = candidate;
        }
        return cleaned;
    }

    /** Cleans the track and computes the final metrics. Empty/single-point tracks yield all zeros. */
    public RunMetrics calculate(List<TrackPoint> rawPoints) {
        List<TrackPoint> points = clean(rawPoints);
        if (points.size() < 2) {
            return RunMetrics.zero();
        }

        double distanceM = 0;
        double maxSegmentSpeedKmh = 0;
        double maxInclinationDeg = 0;
        double inclinationSum = 0;
        int inclinationSegments = 0;

        for (int i = 1; i < points.size(); i++) {
            TrackPoint prev = points.get(i - 1);
            TrackPoint curr = points.get(i);
            double segmentM = Haversine.distanceMeters(prev.lat(), prev.lng(), curr.lat(), curr.lng());
            distanceM += segmentM;

            double seconds = secondsBetween(prev, curr);
            if (seconds > 0) {
                maxSegmentSpeedKmh = Math.max(maxSegmentSpeedKmh, (segmentM / seconds) * 3.6);
            }

            if (prev.altitude() != null && curr.altitude() != null && segmentM > 0) {
                double deltaAlt = curr.altitude() - prev.altitude();
                double angleDeg = Math.toDegrees(Math.atan2(deltaAlt, segmentM));
                double absAngle = Math.abs(angleDeg);
                maxInclinationDeg = Math.max(maxInclinationDeg, absAngle);
                inclinationSum += absAngle;
                inclinationSegments++;
            }
        }

        long durationSec = Duration.between(
                points.get(0).recordedAt(), points.get(points.size() - 1).recordedAt()).getSeconds();

        // Cap chip-reported speeds — a plausible lat/lng with a bogus speedKmh must not
        // inflate max speed (and friend rankings) above the spike threshold.
        double maxSpeedKmh = Math.max(maxSegmentSpeedKmh, maxPlausibleReportedSpeed(points));
        double avgSpeedKmh = durationSec > 0 ? (distanceM / durationSec) * 3.6 : 0.0;
        AltitudeStats altitudeStats = altitudeStats(points);
        double avgInclinationDeg = inclinationSegments > 0 ? inclinationSum / inclinationSegments : 0.0;

        return new RunMetrics(maxSpeedKmh, avgSpeedKmh, distanceM, altitudeStats.maxAltitudeM(),
                altitudeStats.verticalDropM(), maxInclinationDeg, avgInclinationDeg, durationSec);
    }

    private record AltitudeStats(double maxAltitudeM, double verticalDropM) {
        static AltitudeStats empty() {
            return new AltitudeStats(0.0, 0.0);
        }
    }

    private static AltitudeStats altitudeStats(List<TrackPoint> points) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (TrackPoint p : points) {
            if (p.altitude() == null) {
                continue;
            }
            min = Math.min(min, p.altitude());
            max = Math.max(max, p.altitude());
        }
        if (max == Double.NEGATIVE_INFINITY) {
            return AltitudeStats.empty();
        }
        double verticalDropM = min == Double.POSITIVE_INFINITY || Double.compare(min, max) == 0 ? 0.0 : max - min;
        return new AltitudeStats(max, verticalDropM);
    }

    /** Highest chip-reported speed within the plausible ski range; 0 when none. */
    private static double maxPlausibleReportedSpeed(List<TrackPoint> points) {
        return points.stream()
                .map(TrackPoint::speedKmh)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .filter(speed -> speed > 0 && speed <= MAX_SPEED_KMH)
                .max()
                .orElse(0.0);
    }

    private static double secondsBetween(TrackPoint a, TrackPoint b) {
        return Duration.between(a.recordedAt(), b.recordedAt()).toMillis() / 1000.0;
    }
}
